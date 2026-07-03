package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponseDto {

    private Long memberId;
    private String email;
    private String nickname;
    private String role;
    private String accessToken;
    private String refreshToken;
}
