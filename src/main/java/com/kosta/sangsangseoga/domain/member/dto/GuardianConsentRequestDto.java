package com.kosta.sangsangseoga.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuardianConsentRequestDto {

    @Schema(description = "동의를 받아야 하는 미성년(PENDING) 회원 ID")
    private Long memberId;

    @Schema(description = "보호자 이름. 현재 ERD에 저장 컬럼이 없어 검증용으로만 받고 영속하지 않는다.")
    private String guardianName;

    @Schema(description = "보호자 이메일. 이 주소로 동의 요청 메일이 발송된다.")
    private String guardianEmail;
}
