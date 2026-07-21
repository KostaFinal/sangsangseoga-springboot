package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "비밀번호")
    private String password;

    @Schema(description = "true면 Refresh Token을 길게(기본 30일), false/미지정이면 짧게(기본 1일) 발급한다. "
            + "로그인 세션 유지 기간에만 영향을 주고 Access Token 만료(1시간)에는 영향 없다.",
            defaultValue = "false")
    private Boolean rememberMe;
}
