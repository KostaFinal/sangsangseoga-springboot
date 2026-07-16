package com.kosta.sangsangseoga.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SseTicketResponseDto {

    @Schema(description = "실시간 알림 구독(SSE) 연결에 쓰는 1회용 티켓. 발급 후 30초 내에 소비해야 하며, 소비 즉시 무효화된다.")
    private String ticket;
}
