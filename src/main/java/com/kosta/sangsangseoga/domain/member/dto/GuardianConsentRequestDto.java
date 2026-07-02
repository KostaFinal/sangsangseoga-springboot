package com.kosta.sangsangseoga.domain.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuardianConsentRequestDto {

    private Long memberId;
    // 현재 ERD(guardian_consent)에 이름을 저장할 컬럼이 없어 검증용으로만 받고 영속하지 않음.
    private String guardianName;
    private String guardianEmail;
}
