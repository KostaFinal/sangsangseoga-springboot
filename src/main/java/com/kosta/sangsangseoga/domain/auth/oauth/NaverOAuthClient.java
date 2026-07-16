package com.kosta.sangsangseoga.domain.auth.oauth;

import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * 네이버 로그인 연동. https://developers.naver.com/docs/login/api/api.md
 *
 * 주의: 콜백 계약(code, redirectUri만 전달)이 state를 되돌려주지 않아, CSRF 방지용 state는
 * 매번 새로 생성만 하고 검증하지는 않는다(왕복 검증하려면 콜백 계약에 state 필드 추가 필요).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOAuthClient implements OAuthClient {

    private static final String AUTHORIZE_URL = "https://nid.naver.com/oauth2.0/authorize";
    private static final String TOKEN_URL = "https://nid.naver.com/oauth2.0/token";
    private static final String USER_INFO_URL = "https://openapi.naver.com/v1/nid/me";

    // 공용 RestTemplate 빈이 없어져서(다른 도메인도 각자 new RestTemplate() 관례를 따름) 여기서도 직접 생성한다.
    private final RestTemplate restTemplate = new RestTemplate();
    private final NaverOAuthProperties properties;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.NAVER;
    }

    @Override
    public String buildAuthorizeUrl(String redirectUri) {
        requireConfigured();
        return UriComponentsBuilder.fromHttpUrl(AUTHORIZE_URL)
                .queryParam("response_type", "code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", UUID.randomUUID().toString())
                .build()
                .encode()
                .toUriString();
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String redirectUri) {
        requireConfigured();
        String accessToken = exchangeToken(code);
        return fetchProfile(accessToken);
    }

    /**
     * NAVER_CLIENT_ID/SECRET 미설정 시 빈 값으로 네이버 서버까지 요청이 나가 원인 파악이 어려워지므로
     * 여기서 먼저 걸러낸다(서버 기동은 막지 않음). 네이버는 카카오와 달리 client_secret이 항상 필수다.
     */
    private void requireConfigured() {
        if (properties.getClientId() == null || properties.getClientId().isBlank()
                || properties.getClientSecret() == null || properties.getClientSecret().isBlank()) {
            throw new CustomException(AuthErrorCode.OAUTH_NOT_CONFIGURED);
        }
    }

    private String exchangeToken(String code) {
        String url = UriComponentsBuilder.fromHttpUrl(TOKEN_URL)
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", properties.getClientId())
                .queryParam("client_secret", properties.getClientSecret())
                .queryParam("code", code)
                .queryParam("state", UUID.randomUUID().toString())
                .build()
                .encode()
                .toUriString();
        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            Object accessToken = response == null ? null : response.get("access_token");
            if (accessToken == null) {
                throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
            }
            return accessToken.toString();
        } catch (RestClientException e) {
            log.warn("네이버 토큰 교환 실패", e);
            throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfo fetchProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        Map<String, Object> body;
        try {
            Map<?, ?> raw = restTemplate.exchange(
                    USER_INFO_URL, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();
            if (raw == null || !"00".equals(String.valueOf(raw.get("resultcode")))) {
                throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
            }
            body = (Map<String, Object>) raw.get("response");
        } catch (RestClientException e) {
            log.warn("네이버 사용자 정보 조회 실패", e);
            throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
        }

        return OAuthUserInfo.builder()
                .providerId((String) body.get("id"))
                .email((String) body.get("email"))
                .nickname((String) body.get("nickname"))
                .profileImageUrl((String) body.get("profile_image"))
                .birthDate(parseBirthDate((String) body.get("birthyear"), (String) body.get("birthday")))
                .build();
    }

    /** birthyear(YYYY) + birthday(MM-DD) 둘 다 동의된 경우에만 생년월일을 구성한다. 하나라도 없으면 null. */
    private LocalDate parseBirthDate(String birthyear, String birthday) {
        if (birthyear == null || birthyear.isBlank() || birthday == null || !birthday.contains("-")) {
            return null;
        }
        try {
            String[] parts = birthday.split("-");
            return LocalDate.of(Integer.parseInt(birthyear), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        } catch (RuntimeException e) {
            log.warn("네이버 생년월일 파싱 실패: birthyear={}, birthday={}", birthyear, birthday);
            return null;
        }
    }
}
