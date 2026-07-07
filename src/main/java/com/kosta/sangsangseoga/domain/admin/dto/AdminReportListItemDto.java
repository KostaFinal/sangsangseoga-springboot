package com.kosta.sangsangseoga.domain.admin.dto;

import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportReason;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportStatus;
import com.kosta.sangsangseoga.domain.friendLibrary.enums.ReportTargetType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportListItemDto {

    private Long reportId;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private String reasonDetail;
    private ReportStatus status;
    private Long reporterId;
    private String reporterNickname;
    private LocalDateTime createdAt;
}
