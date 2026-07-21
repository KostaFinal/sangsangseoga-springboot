package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GuardianConsentResponseDto {

    @Schema(description = "동의 요청 ID")
    private Long consentId;

    @Schema(description = "동의 상태", example = "APPROVED",
            allowableValues = {"REQUESTED", "APPROVED", "REJECTED", "EXPIRED", "WITHDRAWN"})
    private String status;

    @Schema(description = "동의 요청 만료 시각(생성 후 7일). 만료 후에는 처리 시도 시 EXPIRED로 전환되고 에러가 반환된다.")
    private LocalDateTime expiresAt;
}
