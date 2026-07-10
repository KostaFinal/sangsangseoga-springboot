package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthAuthorizeUrlResponseDto {

    @Schema(description = "프론트가 이동시킬 소셜 로그인 동의 화면 URL. client_id 등 시크릿이 이미 조합되어 있다.")
    private String authorizeUrl;
}
