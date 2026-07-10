package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class OAuthCallbackRequestDto {

    @Schema(description = "소셜 제공자 인가 화면에서 리다이렉트로 돌아올 때 받은 1회용 인가 코드",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "인가 코드(code)가 필요합니다.")
    private String code;

    @Schema(description = "인가 URL 발급 시(GET /authorize-url) 넘겼던 redirectUri와 반드시 동일해야 한다.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "redirectUri가 필요합니다.")
    private String redirectUri;
}
