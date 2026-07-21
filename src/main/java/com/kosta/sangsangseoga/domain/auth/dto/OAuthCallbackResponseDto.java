package com.kosta.sangsangseoga.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * callback / complete-signup 두 API가 공유하는 응답 형태. 케이스별로 채워지는 필드가 다르다.
 * - 로그인 또는 성인 가입(신규/complete-signup 공통): accessToken, refreshToken, memberId, email, nickname, profileImageUrl, role
 * - 미성년 가입(신규/complete-signup 공통): pendingGuardianConsent=true, memberId만
 * - 생년월일 미제공 신규 회원: isNewMember=true, oauthSignupToken, email, nickname, profileImageUrl
 */
@Getter
@Builder
public class OAuthCallbackResponseDto {

    @Schema(description = "회원 ID. isNewMember=true(oauthSignupToken 발급) 케이스에서만 null.", nullable = true)
    private Long memberId;

    @Schema(description = "이메일. pendingGuardianConsent=true 케이스에서만 null.", nullable = true)
    private String email;

    @Schema(description = "닉네임. pendingGuardianConsent=true 케이스에서만 null.", nullable = true)
    private String nickname;

    @Schema(description = "프로필 이미지 URL. pendingGuardianConsent=true 케이스, 또는 제공자가 안 준 경우 null.", nullable = true)
    private String profileImageUrl;

    @Schema(description = "권한. 토큰이 발급되는 케이스(기존 회원/성인 신규가입)에서만 채워짐.", nullable = true,
            example = "USER", allowableValues = {"USER", "ADMIN"})
    private String role;

    @Schema(description = "Access Token. isNewMember=true 또는 pendingGuardianConsent=true 케이스에서는 null.", nullable = true)
    private String accessToken;

    @Schema(description = "Refresh Token. isNewMember=true 또는 pendingGuardianConsent=true 케이스에서는 null.", nullable = true)
    private String refreshToken;

    @Schema(description = "true면 처음 보는 소셜 계정이고 제공자가 생년월일을 안 줘서 complete-signup 호출이 필요하다는 뜻. "
            + "이 경우에만 true이고, 그 외 모든 케이스에서는 필드 자체가 응답에 없다(null).", nullable = true)
    private Boolean isNewMember;

    @Schema(description = "isNewMember=true일 때만 발급되는 1회용 토큰(30분 유효). "
            + "POST .../complete-signup 호출 시 닉네임/생년월일과 함께 그대로 넘겨야 한다.", nullable = true)
    private String oauthSignupToken;

    @Schema(description = "true면 만 14세 미만이라 보호자 동의가 완료돼야 로그인할 수 있다(토큰 미발급). "
            + "이 경우에만 true이고, 그 외 모든 케이스에서는 필드 자체가 응답에 없다(null).", nullable = true)
    private Boolean pendingGuardianConsent;
}
