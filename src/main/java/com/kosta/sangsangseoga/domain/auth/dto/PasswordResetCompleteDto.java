package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetCompleteDto {

    @Schema(description = "비밀번호 재설정 메일에 담긴 토큰(30분 유효, 1회용)")
    private String token;

    @Schema(description = "새 비밀번호. 영문/숫자/특수문자 조합 8자 이상")
    private String newPassword;
}
