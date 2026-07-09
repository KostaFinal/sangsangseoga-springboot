package com.kosta.sangsangseoga.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * callback / complete-signup 두 API가 공유하는 응답 형태. 실제로 채워지는 필드는 케이스마다 다르다.
 *
 * - 기존 회원(로그인)                : accessToken, refreshToken, memberId, email, nickname, profileImageUrl, role
 * - 신규 회원 + 생년월일 미제공        : isNewMember=true, oauthSignupToken, email, nickname, profileImageUrl
 * - 신규 회원 + 생년월일 제공(성인)    : accessToken, refreshToken, memberId, email, nickname, profileImageUrl, role
 * - 신규 회원 + 생년월일 제공(미성년)  : pendingGuardianConsent=true, memberId
 * - complete-signup(성인)           : accessToken, refreshToken, memberId, email, nickname, profileImageUrl, role
 * - complete-signup(미성년)         : pendingGuardianConsent=true, memberId
 */
@Getter
@Builder
public class OAuthCallbackResponseDto {
    private Long memberId;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private String accessToken;
    private String refreshToken;
    private Boolean isNewMember;
    private String oauthSignupToken;
    private Boolean pendingGuardianConsent;
}
