package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.admin.enums.AdminActionType;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportProcessResponseDto {

    private Long reportId;
    private ReportStatus status;
    private AdminActionType actionType;
    private LocalDateTime processedAt;
}
