package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String accessToken;
    private String refreshToken;
}
