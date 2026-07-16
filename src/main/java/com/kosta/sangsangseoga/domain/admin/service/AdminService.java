package com.kosta.sangsangseoga.domain.admin.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.sangsangseoga.domain.admin.dto.AdminActionLogListItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminActionLogListResponseDto;

import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberListItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberStatusChangeRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminMemberStatusChangeResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminTokenTimelineItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminTokenTrendItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminTokenUsageItemDto;
import com.kosta.sangsangseoga.domain.admin.entity.AdminActionLog;
import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.admin.exception.AdminErrorCode;
import com.kosta.sangsangseoga.domain.admin.repository.AdminActionLogRepository;
import com.kosta.sangsangseoga.domain.ai.entity.AiGenerationUsage;
import com.kosta.sangsangseoga.domain.ai.enums.CallType;
import com.kosta.sangsangseoga.domain.ai.repository.AiGenerationUsageRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.enums.BookStatus;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Report;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.ReportRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.enums.MemberRole;
import com.kosta.sangsangseoga.domain.member.enums.MemberStatus;
import com.kosta.sangsangseoga.domain.member.exception.MemberErrorCode;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.event.AfterCommitTask;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import com.kosta.sangsangseoga.global.jwt.RefreshTokenService;
import com.kosta.sangsangseoga.global.jwt.TokenBlacklistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

	private static final int TREND_DAILY_DAYS = 7;
	private static final int TREND_MONTHLY_MONTHS = 5;
	private static final DateTimeFormatter TIMELINE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

	private final ReportRepository reportRepository;
	private final AdminActionLogRepository adminActionLogRepository;
	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;
	private final CommentRepository commentRepository;
	private final RefreshTokenService refreshTokenService;
	private final TokenBlacklistService tokenBlacklistService;
	private final ApplicationEventPublisher eventPublisher;
	private final AiGenerationUsageRepository aiGenerationUsageRepository;

	@Transactional(readOnly = true)
	public AdminReportListResponseDto getReports(ReportStatus status, Pageable pageable) {
		ReportStatus targetStatus = status != null ? status : ReportStatus.PENDING;
		Page<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(targetStatus, pageable);
		List<Report> content = reports.getContent();

		Map<Long, String> authorNicknames = resolveAuthorNicknames(content);
		Map<Long, Long> commentParentBookIds = resolveCommentParentBookIds(content);
		Map<Long, AdminActionLog> resolutionByReportId = resolveResolutionByReportId(content);

		List<AdminReportListItemDto> items = content.stream()
				.map(report -> toListItemDto(report, authorNicknames, commentParentBookIds, resolutionByReportId))
				.collect(Collectors.toList());

		return AdminReportListResponseDto.builder().items(items).totalCount(reports.getTotalElements())
				.page(reports.getNumber()).hasNext(reports.hasNext()).build();
	}

	/**
	 * targetType=AUTHOR인 신고들의 targetId(memberId)를 배치 조회해 닉네임 맵을 만든다. FE의 "신고 대상 확인"
	 * 이동 기능이 항목마다 회원 조회를 따로 하지 않도록 목록 응답에 미리 채워준다.
	 */
	private Map<Long, String> resolveAuthorNicknames(List<Report> reports) {
		List<Long> authorIds = reports.stream().filter(report -> report.getTargetType() == ReportTargetType.AUTHOR)
				.map(Report::getTargetId).distinct().collect(Collectors.toList());
		if (authorIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return memberRepository.findAllById(authorIds).stream()
				.collect(Collectors.toMap(Member::getId, Member::getNickname));
	}

	/**
	 * targetType=COMMENT인 신고들의 targetId(commentId)를 배치 조회해 원본 도서 ID 맵을 만든다. 댓글은 도서
	 * 상세 화면 안에서만 보이므로 FE가 이동하려면 부모 도서 ID가 필요하다.
	 */
	private Map<Long, Long> resolveCommentParentBookIds(List<Report> reports) {
		List<Long> commentIds = reports.stream().filter(report -> report.getTargetType() == ReportTargetType.COMMENT)
				.map(Report::getTargetId).distinct().collect(Collectors.toList());
		if (commentIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return commentRepository.findAllById(commentIds).stream()
				.collect(Collectors.toMap(Comment::getId, comment -> comment.getBook().getId()));
	}

	/**
	 * status=RESOLVED/REJECTED인 신고들의 처리 이력(AdminActionLog)을 reportId 기준으로 배치 조회한다.
	 * 신고 1건은 최대 1번만 처리되므로(재처리 불가) reportId당 로그가 하나뿐이다.
	 */
	private Map<Long, AdminActionLog> resolveResolutionByReportId(List<Report> reports) {
		List<Long> resolvedReportIds = reports.stream().filter(report -> report.getStatus() != ReportStatus.PENDING)
				.map(Report::getId).collect(Collectors.toList());
		if (resolvedReportIds.isEmpty()) {
			return Collections.emptyMap();
		}
		return adminActionLogRepository.findByReportIdInWithAdmin(resolvedReportIds).stream()
				.collect(Collectors.toMap(actionLog -> actionLog.getReport().getId(), actionLog -> actionLog));
	}

	/**
	 * 신고 처리. actionType에 따라 대상(책/댓글/작가)에 실제 조치를 하고, 신고 상태를 갱신한 뒤 AdminActionLog에 처리
	 * 이력을 남긴다. REPORT_REJECT는 대상 조치 없이 신고만 기각한다.
	 */
	public AdminReportProcessResponseDto processReport(Long adminMemberId, Long reportId,
			AdminReportProcessRequestDto request) {
		Member admin = memberRepository.findById(adminMemberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

		Report report = reportRepository.findById(reportId)
				.orElseThrow(() -> new CustomException(AdminErrorCode.REPORT_NOT_FOUND));

		if (report.getStatus() != ReportStatus.PENDING) {
			throw new CustomException(AdminErrorCode.REPORT_ALREADY_PROCESSED);
		}

		AdminActionType actionType = request.getActionType();
		applyAction(report, actionType);

		report.setStatus(actionType == AdminActionType.REPORT_REJECT ? ReportStatus.REJECTED : ReportStatus.RESOLVED);
		report.setProcessedBy(admin);
		report.setProcessedAt(LocalDateTime.now());

		adminActionLogRepository.save(AdminActionLog.builder().report(report).admin(admin).actionType(actionType)
				.actionReason(request.getActionReason()).build());

		return AdminReportProcessResponseDto.builder().reportId(report.getId()).status(report.getStatus())
				.actionType(actionType).processedAt(report.getProcessedAt()).build();
	}

	private void applyAction(Report report, AdminActionType actionType) {
		switch (actionType) {
		case BOOK_HIDE:
			requireTargetType(report, ReportTargetType.BOOK);
			Book book = bookRepository.findById(report.getTargetId())
					.orElseThrow(() -> new CustomException(AdminErrorCode.ACTION_TARGET_NOT_FOUND));
			book.setStatus(BookStatus.HIDDEN);
			break;
		case COMMENT_DELETE:
			requireTargetType(report, ReportTargetType.COMMENT);
			Comment comment = commentRepository.findById(report.getTargetId())
					.orElseThrow(() -> new CustomException(AdminErrorCode.ACTION_TARGET_NOT_FOUND));
			comment.setIsDeleted(true);
			break;
		case AUTHOR_SUSPEND:
			requireTargetType(report, ReportTargetType.AUTHOR);
			Member author = memberRepository.findById(report.getTargetId())
					.orElseThrow(() -> new CustomException(AdminErrorCode.ACTION_TARGET_NOT_FOUND));
			// 이미 탈퇴(DELETED)한 회원은 정지로 되돌리지 않는다 (탈퇴 시 정리된 데이터와 상태가 어긋나는 것 방지)
			if (author.getStatus() == MemberStatus.DELETED) {
				throw new CustomException(MemberErrorCode.ALREADY_DELETED_MEMBER);
			}
			author.suspend();
			invalidateSessionsAfterCommit(author.getId());
			break;
		case REPORT_REJECT:
			// 대상에는 아무 조치도 하지 않고 신고만 기각 처리한다.
			break;
		}
	}

	private void requireTargetType(Report report, ReportTargetType expected) {
		if (report.getTargetType() != expected) {
			throw new CustomException(AdminErrorCode.ACTION_TARGET_TYPE_MISMATCH);
		}
	}

	/**
	 * 관리자에 의해 정지/탈퇴된 회원의 기존 세션을 무효화한다. access token은 발급 시점 기준으로 블랙리스트 처리하고, refresh
	 * token은 Redis에서 삭제해 재발급도 막는다. 트랜잭션이 롤백되면 이 작업도 실행되지 않도록 커밋 이후로 미룬다.
	 */
	private void invalidateSessionsAfterCommit(Long memberId) {
		eventPublisher.publishEvent(new AfterCommitTask(this, () -> {
			tokenBlacklistService.invalidateTokensIssuedBefore(memberId, Instant.now());
			refreshTokenService.delete(memberId);
		}));
	}

	@Transactional(readOnly = true)
	public AdminMemberListResponseDto getMembers(MemberStatus status, String keyword, Pageable pageable) {
		String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
		Page<Member> members = memberRepository.searchForAdmin(status, normalizedKeyword, pageable);

		List<AdminMemberListItemDto> items = members.getContent().stream().map(this::toMemberListItemDto)
				.collect(Collectors.toList());

		return AdminMemberListResponseDto.builder().items(items).totalCount(members.getTotalElements())
				.page(members.getNumber()).hasNext(members.hasNext()).build();
	}

	/**
	 * 회원 상태 강제 변경(정지/정상복원/탈퇴). PENDING(보호자 동의 대기)으로의 전환은 회원가입 흐름 전용이라 허용하지 않는다. 이미
	 * 탈퇴 처리된 회원은 상태를 되돌리지 않는다(탈퇴는 되돌릴 수 없는 처리로 취급).
	 */
	public AdminMemberStatusChangeResponseDto changeMemberStatus(Long adminMemberId, Long memberId,
			AdminMemberStatusChangeRequestDto request) {
		memberRepository.findById(adminMemberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

		Member member = memberRepository.findById(memberId)
				.orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

		// 관리자 본인 포함, 어떤 관리자 계정도 이 API로는 상태를 바꿀 수 없다.
		// (자기 자신 정지, 마지막 남은 관리자 계정 잠금 등의 사고를 원천 차단)
		if (member.getRole() == MemberRole.ADMIN) {
			throw new CustomException(AdminErrorCode.ADMIN_STATUS_CHANGE_NOT_ALLOWED);
		}

		MemberStatus targetStatus = request.getStatus();
		if (targetStatus != MemberStatus.ACTIVE && targetStatus != MemberStatus.SUSPENDED
				&& targetStatus != MemberStatus.DELETED) {
			throw new CustomException(AdminErrorCode.INVALID_TARGET_STATUS);
		}
		if (member.getStatus() == MemberStatus.DELETED) {
			throw new CustomException(MemberErrorCode.ALREADY_DELETED_MEMBER);
		}

		switch (targetStatus) {
		case ACTIVE:
			member.activate();
			break;
		case SUSPENDED:
			member.suspend();
			invalidateSessionsAfterCommit(member.getId());
			break;
		case DELETED:
			member.cancelSubscriptionImmediately();
			member.withdraw();
			invalidateSessionsAfterCommit(member.getId());
			break;
		}

		log.info("관리자[{}]가 회원[{}] 상태를 {}로 변경. 사유: {}", adminMemberId, memberId, targetStatus, request.getReason());

		return AdminMemberStatusChangeResponseDto.builder().memberId(member.getId()).status(member.getStatus())
				.processedAt(LocalDateTime.now()).build();
	}

	@Transactional(readOnly = true)
	public AdminActionLogListResponseDto getActionLogs(AdminActionType actionType, Pageable pageable) {
		Page<AdminActionLog> logs = actionType != null
				? adminActionLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType, pageable)
				: adminActionLogRepository.findAllByOrderByCreatedAtDesc(pageable);
		List<Report> reports = logs.getContent().stream().map(AdminActionLog::getReport).collect(Collectors.toList());

		Map<Long, String> authorNicknames = resolveAuthorNicknames(reports);
		Map<Long, Long> commentParentBookIds = resolveCommentParentBookIds(reports);

		List<AdminActionLogListItemDto> items = logs.getContent().stream()
				.map(actionLog -> toActionLogListItemDto(actionLog, authorNicknames, commentParentBookIds))
				.collect(Collectors.toList());

		return AdminActionLogListResponseDto.builder().items(items).totalCount(logs.getTotalElements())
				.page(logs.getNumber()).hasNext(logs.hasNext()).build();
	}

	private AdminActionLogListItemDto toActionLogListItemDto(AdminActionLog actionLog,
			Map<Long, String> authorNicknames, Map<Long, Long> commentParentBookIds) {
		Report report = actionLog.getReport();
		Member admin = actionLog.getAdmin();
		ReportTargetType targetType = report.getTargetType();
		return AdminActionLogListItemDto.builder().actionLogId(actionLog.getId()).reportId(report.getId())
				.targetType(targetType).targetId(report.getTargetId())
				.targetNickname(
						targetType == ReportTargetType.AUTHOR ? authorNicknames.get(report.getTargetId()) : null)
				.targetParentBookId(
						targetType == ReportTargetType.COMMENT ? commentParentBookIds.get(report.getTargetId()) : null)
				.adminId(admin.getId()).adminNickname(admin.getNickname()).actionType(actionLog.getActionType())
				.actionReason(actionLog.getActionReason()).createdAt(actionLog.getCreatedAt()).build();
	}

	/**
	 * unit=daily는 일별, unit=monthly는 월별 구간을 프리미엄/일반 회원 x 텍스트/이미지로 집계한다. 구간은 실제 사용
	 * 이력이 없어도 0으로 채워서 반환한다(그래프가 빈 구간에서 끊기지 않도록). premiumTxt/freeTxt는 FastAPI가 실제
	 * Gemini 토큰 수(result.usage)를 내려준 값을 만 토큰 단위로 환산한 것이다. FastAPI가 usage를 안 내려준 옛
	 * 호출 이력은 요청/응답 JSON 문자 길이 근사치가 대신 들어가 있을 수 있다.
	 *
	 * - daily: year+month를 함께 주면 그 달의 1일~말일 전체, 생략하면 오늘 기준 최근 7일. - monthly: year를
	 * 주면 그 해의 1월~12월(year가 months보다 우선), year 없이 months만 주면 오늘 기준 최근 months개월, 둘 다
	 * 생략하면 최근 5개월.
	 */
	@Transactional(readOnly = true)
	public List<AdminTokenTrendItemDto> getTokenTrends(String unit, Integer year, Integer month, Integer months) {
		boolean monthly = "monthly".equalsIgnoreCase(unit);

		LinkedHashMap<String, double[]> buckets = new LinkedHashMap<>();
		LocalDateTime from;
		LocalDateTime to;

		if (monthly) {
			if (year != null) {
				for (int m = 1; m <= 12; m++) {
					buckets.put(monthlyBucketKey(YearMonth.of(year, m)), new double[4]);
				}
				from = YearMonth.of(year, 1).atDay(1).atStartOfDay();
				to = YearMonth.of(year, 12).plusMonths(1).atDay(1).atStartOfDay();
			} else {
				int bucketCount = months != null ? months : TREND_MONTHLY_MONTHS;
				for (int i = bucketCount - 1; i >= 0; i--) {
					buckets.put(monthlyBucketKey(YearMonth.now().minusMonths(i)), new double[4]);
				}
				from = YearMonth.now().minusMonths(bucketCount - 1L).atDay(1).atStartOfDay();
				to = YearMonth.now().plusMonths(1).atDay(1).atStartOfDay();
			}
		} else {
			if (year != null && month != null) {
				YearMonth targetMonth = YearMonth.of(year, month);
				for (int d = 1; d <= targetMonth.lengthOfMonth(); d++) {
					buckets.put(targetMonth.atDay(d).toString(), new double[4]);
				}
				from = targetMonth.atDay(1).atStartOfDay();
				to = targetMonth.plusMonths(1).atDay(1).atStartOfDay();
			} else {
				for (int i = TREND_DAILY_DAYS - 1; i >= 0; i--) {
					buckets.put(LocalDate.now().minusDays(i).toString(), new double[4]);
				}
				from = LocalDate.now().minusDays(TREND_DAILY_DAYS - 1L).atStartOfDay();
				to = LocalDate.now().plusDays(1).atStartOfDay();
			}
		}

		for (AiGenerationUsage usage : aiGenerationUsageRepository.findAllWithMemberBetween(from, to)) {
			LocalDate date = usage.getCreatedAt().toLocalDate();
			String key = monthly ? monthlyBucketKey(YearMonth.from(date)) : date.toString();
			double[] bucket = buckets.get(key);
			if (bucket == null) {
				continue;
			}

			boolean premium = usage.getMember().getSubscriptionPlan().isPremium();
			if (usage.getCallType() == CallType.TEXT) {
				int length = usage.getOutputTokenCount() != null ? usage.getOutputTokenCount() : 0;
				bucket[premium ? 0 : 1] += length;
			} else {
				int count = usage.getImageCount() != null ? usage.getImageCount() : 0;
				bucket[premium ? 2 : 3] += count;
			}
		}

		List<AdminTokenTrendItemDto> result = new ArrayList<>();
		for (Map.Entry<String, double[]> entry : buckets.entrySet()) {
			double[] v = entry.getValue();
			result.add(AdminTokenTrendItemDto.builder()
					.label(monthly ? (entry.getKey().split("-")[1] + "월")
							: LocalDate.parse(entry.getKey()).format(DateTimeFormatter.ofPattern("MM/dd")))
					.premiumTxt(roundToOneDecimal(v[0] / 10_000.0)).freeTxt(roundToOneDecimal(v[1] / 10_000.0))
					.premiumImg((int) v[2]).freeImg((int) v[3]).build());
		}
		return result;
	}

	private String monthlyBucketKey(YearMonth yearMonth) {
		return yearMonth.getYear() + "-" + yearMonth.getMonthValue();
	}

	private double roundToOneDecimal(double value) {
		return Math.round(value * 10) / 10.0;
	}

	/**
	 * 회원별 AI 사용량 누적 랭킹. 어뷰징 판정 로직이 아직 없어 status는 항상 NORMAL로 고정한다.
	 */
	@Transactional(readOnly = true)
	public List<AdminTokenUsageItemDto> getTokenUsages() {
		Map<Long, Member> members = new LinkedHashMap<>();
		Map<Long, Long> textUsageByMember = new LinkedHashMap<>();
		Map<Long, Long> imgUsageByMember = new LinkedHashMap<>();

		for (AiGenerationUsage usage : aiGenerationUsageRepository.findAllWithMember()) {
			Member member = usage.getMember();
			members.putIfAbsent(member.getId(), member);
			if (usage.getCallType() == CallType.TEXT) {
				long length = usage.getOutputTokenCount() != null ? usage.getOutputTokenCount() : 0;
				textUsageByMember.merge(member.getId(), length, Long::sum);
			} else {
				long count = usage.getImageCount() != null ? usage.getImageCount() : 0;
				imgUsageByMember.merge(member.getId(), count, Long::sum);
			}
		}

		List<AdminTokenUsageItemDto> result = members.values().stream()
				.map(member -> AdminTokenUsageItemDto.builder().userId(String.valueOf(member.getId()))
						.nickname(member.getNickname())
						.plan(member.getSubscriptionPlan().isPremium() ? "PREMIUM" : "FREE")
						.textUsage(textUsageByMember.getOrDefault(member.getId(), 0L))
						.imgUsage(imgUsageByMember.getOrDefault(member.getId(), 0L)).status("NORMAL").build())
				.collect(Collectors.toList());

		result.sort((a, b) -> Long.compare(b.getTextUsage() + b.getImgUsage(), a.getTextUsage() + a.getImgUsage()));
		return result;
	}

	@Transactional(readOnly = true)
	public List<AdminTokenTimelineItemDto> getTokenUsageTimeline(Long memberId) {
		memberRepository.findById(memberId).orElseThrow(() -> new CustomException(CommonErrorCode.MEMBER_NOT_FOUND));

		return aiGenerationUsageRepository.findByMember_IdOrderByCreatedAtDesc(memberId).stream()
				.map(this::toTokenTimelineItemDto).collect(Collectors.toList());
	}

	private AdminTokenTimelineItemDto toTokenTimelineItemDto(AiGenerationUsage usage) {
		boolean isText = usage.getCallType() == CallType.TEXT;
		String amount = isText
				? String.format("%,d 토큰", usage.getOutputTokenCount() != null ? usage.getOutputTokenCount() : 0)
				: String.format("%,d 장", usage.getImageCount() != null ? usage.getImageCount() : 0);

		return AdminTokenTimelineItemDto.builder().date(usage.getCreatedAt().format(TIMELINE_DATE_FORMAT))
				.action(tokenActionLabel(usage)).usage(isText ? "text" : "image").amount(amount).build();
	}

	/** 관리자 타임라인은 text/image 구분만 필요해 callType 기준의 일반 라벨만 내려준다. */
	private String tokenActionLabel(AiGenerationUsage usage) {
		return usage.getCallType() == CallType.IMAGE ? "이미지 생성" : "AI 텍스트 생성";
	}

	private AdminMemberListItemDto toMemberListItemDto(Member member) {
		return AdminMemberListItemDto.builder().memberId(member.getId()).email(member.getEmail())
				.nickname(member.getNickname()).status(member.getStatus()).role(member.getRole())
				.subscriptionPlan(member.getSubscriptionPlan()).createdAt(member.getCreatedAt())
				.withdrawnAt(member.getWithdrawnAt()).build();
	}

	private AdminReportListItemDto toListItemDto(Report report, Map<Long, String> authorNicknames,
			Map<Long, Long> commentParentBookIds, Map<Long, AdminActionLog> resolutionByReportId) {
		Member reporter = report.getReporter();
		ReportTargetType targetType = report.getTargetType();
		AdminActionLog resolution = resolutionByReportId.get(report.getId());
		return AdminReportListItemDto.builder().reportId(report.getId()).targetType(targetType)
				.targetId(report.getTargetId())
				.targetNickname(
						targetType == ReportTargetType.AUTHOR ? authorNicknames.get(report.getTargetId()) : null)
				.targetParentBookId(
						targetType == ReportTargetType.COMMENT ? commentParentBookIds.get(report.getTargetId()) : null)
				.reason(report.getReason()).reasonDetail(report.getReasonDetail()).status(report.getStatus())
				.resolvedReason(resolution != null ? resolution.getActionReason() : null)
				.resolvedByNickname(resolution != null ? resolution.getAdmin().getNickname() : null)
				.reporterId(reporter.getId()).reporterNickname(reporter.getNickname()).createdAt(report.getCreatedAt())
				.build();
	}
}
