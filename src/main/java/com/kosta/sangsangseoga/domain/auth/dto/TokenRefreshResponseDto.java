package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponseDto {

    @Schema(description = "새로 발급된 Access Token. Refresh Token은 갱신되지 않으므로 기존 것을 계속 사용한다.")
    private String accessToken;
}
