package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PasswordResetRequestDto {

    @Schema(description = "비밀번호를 재설정할 계정의 이메일. 인증 링크가 담긴 메일이 발송된다.")
    private String email;
}
