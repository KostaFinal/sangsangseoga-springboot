package com.kosta.sangsangseoga.domain.member.dto;

import com.kosta.sangsangseoga.domain.member.enums.GuardianConsentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuardianConsentApproveRequestDto {

    private String token;
    // APPROVED 또는 REJECTED만 허용 (서비스단에서 검증)
    private GuardianConsentStatus status;
}
