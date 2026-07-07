package com.kosta.sangsangseoga.domain.admin.service;

import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListItemDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportListResponseDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessRequestDto;
import com.kosta.sangsangseoga.domain.admin.dto.AdminReportProcessResponseDto;
import com.kosta.sangsangseoga.domain.admin.entity.AdminActionLog;
import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.admin.exception.AdminErrorCode;
import com.kosta.sangsangseoga.domain.admin.repository.AdminActionLogRepository;
import com.kosta.sangsangseoga.domain.book.entity.Book;
import com.kosta.sangsangseoga.domain.book.repository.BookRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Comment;
import com.kosta.sangsangseoga.domain.friendLibrary.entity.Report;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.CommentRepository;
import com.kosta.sangsangseoga.domain.friendLibrary.repository.ReportRepository;
import com.kosta.sangsangseoga.domain.member.entity.Member;
import com.kosta.sangsangseoga.domain.member.repository.MemberRepository;
import com.kosta.sangsangseoga.global.exception.CommonErrorCode;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final ReportRepository reportRepository;
    private final AdminActionLogRepository adminActionLogRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public AdminReportListResponseDto getPendingReports(Pageable pageable) {
        Page<Report> reports = reportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.PENDING, pageable);

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
                book.setStatus("HIDDEN");
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
                author.suspend();
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
