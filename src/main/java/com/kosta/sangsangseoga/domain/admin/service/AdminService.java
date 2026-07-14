package com.kosta.sangsangseoga.domain.admin.service;

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
import com.kosta.sangsangseoga.domain.admin.entity.AdminActionLog;
import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.admin.exception.AdminErrorCode;
import com.kosta.sangsangseoga.domain.admin.repository.AdminActionLogRepository;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final ReportRepository reportRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public AdminReportListResponseDto getReports(ReportStatus status, Pageable pageable) {
        Page<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);

        List<AdminReportListItemDto> items = reports.getContent().stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());

        return AdminReportListResponseDto.builder()
                .items(items)
                .totalCount(reports.getTotalElements())
                .page(reports.getNumber())
                .hasNext(reports.hasNext())
                .build();
    }

    /**
     * 신고 처리. actionType에 따라 대상(책/댓글/작가)에 실제 조치를 하고, 신고 상태를 갱신한 뒤
     * AdminActionLog에 처리 이력을 남긴다. REPORT_REJECT는 대상 조치 없이 신고만 기각한다.
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

        adminActionLogRepository.save(AdminActionLog.builder()
                .report(report)
                .admin(admin)
                .actionType(actionType)
                .actionReason(request.getActionReason())
                .build());

        return AdminReportProcessResponseDto.builder()
                .reportId(report.getId())
                .status(report.getStatus())
                .actionType(actionType)
                .processedAt(report.getProcessedAt())
                .build();
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
     * 관리자에 의해 정지/탈퇴된 회원의 기존 세션을 무효화한다. access token은 발급 시점 기준으로
     * 블랙리스트 처리하고, refresh token은 Redis에서 삭제해 재발급도 막는다. 트랜잭션이 롤백되면
     * 이 작업도 실행되지 않도록 커밋 이후로 미룬다.
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

        List<AdminMemberListItemDto> items = members.getContent().stream()
                .map(this::toMemberListItemDto)
                .collect(Collectors.toList());

        return AdminMemberListResponseDto.builder()
                .items(items)
                .totalCount(members.getTotalElements())
                .page(members.getNumber())
                .hasNext(members.hasNext())
                .build();
    }

    /**
     * 회원 상태 강제 변경(정지/정상복원/탈퇴). PENDING(보호자 동의 대기)으로의 전환은 회원가입 흐름 전용이라 허용하지 않는다.
     * 이미 탈퇴 처리된 회원은 상태를 되돌리지 않는다(탈퇴는 되돌릴 수 없는 처리로 취급).
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
        if (targetStatus != MemberStatus.ACTIVE
                && targetStatus != MemberStatus.SUSPENDED
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

        log.info("관리자[{}]가 회원[{}] 상태를 {}로 변경. 사유: {}",
                adminMemberId, memberId, targetStatus, request.getReason());

        return AdminMemberStatusChangeResponseDto.builder()
                .memberId(member.getId())
                .status(member.getStatus())
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public AdminActionLogListResponseDto getActionLogs(Pageable pageable) {
        Page<AdminActionLog> logs = adminActionLogRepository.findAllByOrderByCreatedAtDesc(pageable);

        List<AdminActionLogListItemDto> items = logs.getContent().stream()
                .map(this::toActionLogListItemDto)
                .collect(Collectors.toList());

        return AdminActionLogListResponseDto.builder()
                .items(items)
                .totalCount(logs.getTotalElements())
                .page(logs.getNumber())
                .hasNext(logs.hasNext())
                .build();
    }

    private AdminActionLogListItemDto toActionLogListItemDto(AdminActionLog actionLog) {
        Report report = actionLog.getReport();
        Member admin = actionLog.getAdmin();
        return AdminActionLogListItemDto.builder()
                .actionLogId(actionLog.getId())
                .reportId(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .adminId(admin.getId())
                .adminNickname(admin.getNickname())
                .actionType(actionLog.getActionType())
                .actionReason(actionLog.getActionReason())
                .createdAt(actionLog.getCreatedAt())
                .build();
    }

    private AdminMemberListItemDto toMemberListItemDto(Member member) {
        return AdminMemberListItemDto.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .status(member.getStatus())
                .role(member.getRole())
                .subscriptionPlan(member.getSubscriptionPlan())
                .createdAt(member.getCreatedAt())
                .withdrawnAt(member.getWithdrawnAt())
                .build();
    }

    private AdminReportListItemDto toListItemDto(Report report) {
        Member reporter = report.getReporter();
        return AdminReportListItemDto.builder()
                .reportId(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .reasonDetail(report.getReasonDetail())
                .status(report.getStatus())
                .reporterId(reporter.getId())
                .reporterNickname(reporter.getNickname())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
