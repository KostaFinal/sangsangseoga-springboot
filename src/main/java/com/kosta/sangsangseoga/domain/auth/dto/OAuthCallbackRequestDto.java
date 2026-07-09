package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class OAuthCallbackRequestDto {

    @NotBlank(message = "인가 코드(code)가 필요합니다.")
    private String code;

    @NotBlank(message = "redirectUri가 필요합니다.")
    private String redirectUri;
}
