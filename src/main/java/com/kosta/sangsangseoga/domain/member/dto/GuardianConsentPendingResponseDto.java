package com.kosta.sangsangseoga.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GuardianConsentPendingResponseDto {

    private Long consentId;
    private Long memberId;
    private String memberNickname;
    private String memberEmail;
    private LocalDate memberBirthDate;
    private LocalDateTime requestedAt;
    private LocalDateTime expiresAt;
}
