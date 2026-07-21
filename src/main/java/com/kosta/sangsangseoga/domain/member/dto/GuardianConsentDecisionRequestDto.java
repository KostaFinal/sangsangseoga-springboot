package com.kosta.sangsangseoga.domain.member.dto;

import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuardianConsentDecisionRequestDto {

    @Schema(description = "APPROVED 또는 REJECTED만 허용된다(그 외 값은 400 처리)", allowableValues = {"APPROVED", "REJECTED"})
    private GuardianConsentStatus status;
}
