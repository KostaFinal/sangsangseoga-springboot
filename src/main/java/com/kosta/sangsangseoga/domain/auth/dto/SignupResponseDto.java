package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 만 14세 이상(즉시 ACTIVE)이면 accessToken/refreshToken이 채워지고, 만 14세 미만(PENDING)이면
 * 토큰 없이 pendingGuardianConsent=true만 내려간다(보호자 동의 없이 API를 쓸 수 있게 되는 것을 막기 위함).
 */
@Getter
@Builder
public class SignupResponseDto {

    @Schema(description = "회원 ID")
    private Long memberId;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "권한", example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    @Schema(description = "Access Token. 만 14세 미만(보호자 동의 대기)으로 가입된 경우 null", nullable = true)
    private String accessToken;

    @Schema(description = "Refresh Token. 만 14세 미만(보호자 동의 대기)으로 가입된 경우 null", nullable = true)
    private String refreshToken;

    @Schema(description = "true면 만 14세 미만이라 보호자 동의가 완료돼야 로그인할 수 있다(이 경우 토큰은 발급되지 않음). "
            + "만 14세 이상으로 가입되어 토큰이 발급된 경우엔 이 필드 자체가 응답에 없다.", nullable = true)
    private Boolean pendingGuardianConsent;
}
