package com.kosta.sangsangseoga.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationDto {

    @Schema(description = "알림 ID")
    private Long id;

    @Schema(description = "알림 내용")
    private String text;

    @Schema(description = "알림 발생 시각")
    private LocalDateTime createdAt;

    @Schema(description = "읽음 여부")
    private boolean read;
}
