package com.kosta.sangsangseoga.domain.auth.oauth;

import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.Map;

/**
 * 카카오 로그인(REST API) 연동. https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthClient implements OAuthClient {

    private static final String AUTHORIZE_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    // 공용 RestTemplate 빈이 없어져서(다른 도메인도 각자 new RestTemplate() 관례를 따름) 여기서도 직접 생성한다.
    private final RestTemplate restTemplate = new RestTemplate();
    private final KakaoOAuthProperties properties;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public String buildAuthorizeUrl(String redirectUri) {
        requireConfigured();
        return UriComponentsBuilder.fromHttpUrl(AUTHORIZE_URL)
                .queryParam("client_id", properties.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .build()
                .encode()
                .toUriString();
    }

    @Override
    public OAuthUserInfo fetchUserInfo(String code, String redirectUri) {
        requireConfigured();
        String accessToken = exchangeToken(code, redirectUri);
        return fetchProfile(accessToken);
    }

    /**
     * KAKAO_CLIENT_ID 미설정 시 빈 값으로 카카오 서버까지 요청이 나가 원인 파악이 어려워지므로 여기서
     * 먼저 걸러낸다. 소셜 로그인은 선택 기능이라 서버 기동 자체는 막지 않는다.
     */
    private void requireConfigured() {
        if (properties.getClientId() == null || properties.getClientId().isBlank()) {
            throw new CustomException(AuthErrorCode.OAUTH_NOT_CONFIGURED);
        }
    }

    private String exchangeToken(String code, String redirectUri) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("client_id", properties.getClientId());
        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            form.add("client_secret", properties.getClientSecret());
        }
        form.add("redirect_uri", redirectUri);
        form.add("code", code);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            Map<?, ?> response = restTemplate.postForObject(TOKEN_URL, new HttpEntity<>(form, headers), Map.class);
            Object accessToken = response == null ? null : response.get("access_token");
            if (accessToken == null) {
                throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
            }
            return accessToken.toString();
        } catch (RestClientException e) {
            log.warn("카카오 토큰 교환 실패", e);
            throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
        }
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfo fetchProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        Map<String, Object> body;
        try {
            Map<?, ?> response = restTemplate.exchange(
                    USER_INFO_URL, HttpMethod.GET, new HttpEntity<>(headers), Map.class).getBody();
            if (response == null) {
                throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
            }
            body = (Map<String, Object>) response;
        } catch (RestClientException e) {
            log.warn("카카오 사용자 정보 조회 실패", e);
            throw new CustomException(AuthErrorCode.OAUTH_AUTH_FAILED);
        }

        Object id = body.get("id");
        Map<String, Object> kakaoAccount = (Map<String, Object>) body.getOrDefault("kakao_account", Map.of());
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.getOrDefault("profile", Map.of());

        return OAuthUserInfo.builder()
                .providerId(String.valueOf(id))
                .email((String) kakaoAccount.get("email"))
                .nickname((String) profile.get("nickname"))
                .profileImageUrl((String) profile.get("profile_image_url"))
                .birthDate(parseBirthDate(kakaoAccount))
                .build();
    }

    /** birthday(MMDD) + birthyear(YYYY) 둘 다 동의된 경우에만 생년월일을 구성한다. 하나라도 없으면 null. */
    private LocalDate parseBirthDate(Map<String, Object> kakaoAccount) {
        String birthday = (String) kakaoAccount.get("birthday"); // "MMDD"
        String birthyear = (String) kakaoAccount.get("birthyear"); // "YYYY"
        if (birthday == null || birthday.length() != 4 || birthyear == null || birthyear.length() != 4) {
            return null;
        }
        try {
            int month = Integer.parseInt(birthday.substring(0, 2));
            int day = Integer.parseInt(birthday.substring(2, 4));
            return LocalDate.of(Integer.parseInt(birthyear), month, day);
        } catch (RuntimeException e) {
            log.warn("카카오 생년월일 파싱 실패: birthday={}, birthyear={}", birthday, birthyear);
            return null;
        }
    }
}
