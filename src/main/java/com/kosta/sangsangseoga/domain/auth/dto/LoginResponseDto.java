package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", nullable = true)
    private String profileImageUrl;

    @Schema(description = "권한", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    @Schema(description = "Access Token. Authorization: Bearer {accessToken} 형식으로 인증이 필요한 API 호출 시 사용")
    private String accessToken;

    @Schema(description = "Refresh Token. Access Token 재발급(POST /api/auth/token-refresh)에 사용")
    private String refreshToken;
}
