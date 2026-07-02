package com.kosta.sangsangseoga.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GuardianConsentResponseDto {

    private Long consentId;
    private String status;
    private LocalDateTime expiresAt;
}
