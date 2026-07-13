package com.kosta.sangsangseoga.domain.auth.oauth;

import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;

/**
 * 소셜 로그인 제공자(카카오/네이버)별 구현체가 공통으로 맞춰야 하는 계약.
 * client_id/secret은 서버(OAuthProperties)가 들고 있고 프론트에는 절대 노출하지 않는다.
 */
public interface OAuthClient {

    AuthProvider getProvider();

    /** 프론트가 이동시킬 소셜 로그인 동의 화면 URL. client_id 등 시크릿을 조합해 완성된 URL을 돌려준다. */
    String buildAuthorizeUrl(String redirectUri);

    /** 인가 코드(code)로 토큰을 교환하고, 그 토큰으로 사용자 정보를 조회해 정규화한 값으로 돌려준다. */
    OAuthUserInfo fetchUserInfo(String code, String redirectUri);
}
