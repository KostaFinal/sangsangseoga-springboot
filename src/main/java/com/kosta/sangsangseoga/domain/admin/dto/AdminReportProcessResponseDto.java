package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportProcessResponseDto {

    @Schema(description = "신고 ID")
    private Long reportId;

    @Schema(description = "처리 후 신고 상태. REPORT_REJECT면 REJECTED, 그 외 조치면 RESOLVED.")
    private ReportStatus status;

    @Schema(description = "적용된 처리 방식")
    private AdminActionType actionType;

    @Schema(description = "처리 시각")
    private LocalDateTime processedAt;
}
