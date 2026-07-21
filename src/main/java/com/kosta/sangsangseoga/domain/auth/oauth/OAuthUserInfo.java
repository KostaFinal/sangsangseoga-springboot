package com.kosta.sangsangseoga.domain.auth.oauth;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 소셜 제공자(카카오/네이버) 사용자 정보 조회 결과를 provider별 원본 JSON 응답에서 뽑아 정규화한 값.
 * birthDate는 제공자가 생년월일 관련 동의 항목을 안 줬거나 사용자가 동의하지 않았으면 null이다.
 */
@Getter
@Builder
public class OAuthUserInfo {
    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final LocalDate birthDate;
}
