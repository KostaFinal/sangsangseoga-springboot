package com.kosta.sangsangseoga.domain.auth.oauth;

import com.kosta.sangsangseoga.domain.auth.exception.AuthErrorCode;
import com.kosta.sangsangseoga.domain.member.enums.AuthProvider;
import com.kosta.sangsangseoga.global.exception.CustomException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * "kakao"/"naver" 같은 경로 문자열을 실제 OAuthClient 구현체로 연결한다.
 * 새 제공자를 추가하려면 OAuthClient 구현체만 하나 더 등록하면 되고, 이 클래스는 손댈 필요 없다.
 */
@Component
public class OAuthClientResolver {

    private final Map<AuthProvider, OAuthClient> clients;

    public OAuthClientResolver(List<OAuthClient> clients) {
        this.clients = clients.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, Function.identity()));
    }

    public AuthProvider resolveProvider(String providerName) {
        AuthProvider provider;
        try {
            provider = AuthProvider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        if (!clients.containsKey(provider)) {
            throw new CustomException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return provider;
    }

    public OAuthClient resolve(AuthProvider provider) {
        OAuthClient client = clients.get(provider);
        if (client == null) {
            throw new CustomException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return client;
    }
}
