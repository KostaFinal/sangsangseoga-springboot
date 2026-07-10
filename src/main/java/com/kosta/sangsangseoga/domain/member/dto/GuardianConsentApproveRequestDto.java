package com.kosta.sangsangseoga.domain.member.dto;

import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuardianConsentApproveRequestDto {

    @Schema(description = "동의 요청 메일에 담긴 목적 한정 토큰")
    private String token;

    @Schema(description = "APPROVED 또는 REJECTED만 허용된다(그 외 값은 400 처리)", allowableValues = {"APPROVED", "REJECTED"})
    private GuardianConsentStatus status;
}
