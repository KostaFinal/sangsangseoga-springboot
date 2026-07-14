package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminActionLogListItemDto {

    @Schema(description = "처리 이력 ID")
    private Long actionLogId;

    @Schema(description = "처리 대상이 된 신고 ID")
    private Long reportId;

    @Schema(description = "신고 대상 종류")
    private ReportTargetType targetType;

    @Schema(description = "신고 대상 ID")
    private Long targetId;

    @Schema(description = "처리한 관리자 ID")
    private Long adminId;

    @Schema(description = "처리한 관리자 닉네임")
    private String adminNickname;

    @Schema(description = "처리 조치 종류")
    private AdminActionType actionType;

    @Schema(description = "처리 사유", nullable = true)
    private String actionReason;

    @Schema(description = "처리 시각")
    private LocalDateTime createdAt;
}
