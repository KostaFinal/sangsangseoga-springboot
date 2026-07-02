package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponseDto {

    private String accessToken;
}
