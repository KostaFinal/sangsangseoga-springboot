package com.kosta.sangsangseoga.domain.auth.oauth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.naver")
public class NaverOAuthProperties {
    private String clientId;
    private String clientSecret;
}
