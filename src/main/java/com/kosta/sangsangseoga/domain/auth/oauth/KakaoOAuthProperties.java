package com.kosta.sangsangseoga.domain.auth.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {
    private String clientId;
    /** 카카오는 client_secret이 선택 항목(콘솔에서 활성화한 경우에만 필요)이라 비어 있어도 된다. */
    private String clientSecret;
}
