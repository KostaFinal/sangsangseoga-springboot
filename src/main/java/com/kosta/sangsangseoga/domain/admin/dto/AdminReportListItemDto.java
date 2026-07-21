package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportReason;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportListItemDto {

    @Schema(description = "신고 ID")
    private Long reportId;

    @Schema(description = "신고 대상 종류. targetId가 가리키는 테이블을 결정한다(BOOK->book.id, COMMENT->comment.id, AUTHOR->member.id).")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 ID. targetType에 따라 의미가 다르다.")
    private Long targetId;

    @Schema(description = "targetType=COMMENT일 때만 채워짐. 댓글이 달린 원본 도서 ID(이동용). 대상 댓글이 이미 삭제됐으면 null.", nullable = true)
    private Long targetParentBookId;

    @Schema(description = "신고 사유 분류")
    private ReportReason reason;

    @Schema(description = "신고 사유 상세(자유 텍스트)", nullable = true)
    private String reasonDetail;

    @Schema(description = "신고 상태")
    private ReportStatus status;

    @Schema(description = "처리 사유. status=RESOLVED/REJECTED일 때만 채워짐(action-logs의 처리 이력에서 가져옴). PENDING이면 null.", nullable = true)
    private String resolvedReason;

    @Schema(description = "처리한 관리자 닉네임. status=RESOLVED/REJECTED일 때만 채워짐. PENDING이면 null.", nullable = true)
    private String resolvedByNickname;

    @Schema(description = "신고한 회원 ID")
    private Long reporterId;

    @Schema(description = "신고한 회원 닉네임")
    private String reporterNickname;

    @Schema(description = "신고 접수 시각")
    private LocalDateTime createdAt;
}
