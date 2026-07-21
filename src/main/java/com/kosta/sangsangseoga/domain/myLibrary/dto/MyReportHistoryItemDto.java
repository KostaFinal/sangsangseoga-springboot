package com.kosta.sangsangseoga.domain.myLibrary.dto;

import java.time.LocalDateTime;

import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportReason;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyReportHistoryItemDto {
	// 신고 ID
    private Long reportId;

    // BOOK / COMMENT / AUTHOR
    private ReportTargetType targetType;

    // 책 ID / 댓글 ID / 작가 회원 ID
    private Long targetId;

    // 댓글이 작성된 책 ID
    // COMMENT일 때만 값이 들어감
    private Long targetParentBookId;

    // 화면에 표시할 신고 대상 제목
    // 책 제목 / 댓글이 작성된 책 제목 / 작가 닉네임
    private String targetTitle;

    // 화면에 표시할 내용
    // 책 소개 / 댓글 내용 / 작가 소개
    private String targetContent;

    // 신고 사유
    private ReportReason reason;

    // 사용자가 작성한 상세 신고 사유
    private String reasonDetail;

    // PENDING / RESOLVED / REJECTED
    private ReportStatus status;

    // BOOK_HIDE / COMMENT_DELETE / AUTHOR_SUSPEND / REPORT_REJECT
    // 처리 전이면 null
    private AdminActionType actionType;

    // 관리자가 입력한 처리 사유
    private String resolvedReason;

    // 신고 접수 시각
    private LocalDateTime createdAt;

    // 관리자 처리 시각
    private LocalDateTime processedAt;
}
