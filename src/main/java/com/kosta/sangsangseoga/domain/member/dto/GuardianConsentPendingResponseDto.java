package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class GuardianConsentPendingResponseDto {

    @Schema(description = "동의 요청 ID. 승인/거절 처리(PATCH /api/guardian-consents/{consentId}/decision) 시 사용")
    private Long consentId;

    @Schema(description = "동의가 필요한 미성년 회원 ID")
    private Long memberId;

    @Schema(description = "미성년 회원 닉네임")
    private String memberNickname;

    @Schema(description = "미성년 회원 이메일")
    private String memberEmail;

    @Schema(description = "미성년 회원 생년월일")
    private LocalDate memberBirthDate;

    @Schema(description = "동의 요청 생성 시각")
    private LocalDateTime requestedAt;

    @Schema(description = "동의 요청 만료 시각(생성 후 7일)")
    private LocalDateTime expiresAt;
}
