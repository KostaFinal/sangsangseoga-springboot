package com.kosta.sangsangseoga.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey = "defaultSecretKeySecretKeySecretKeySecretKeySecretKey";
    private long accessTokenExpiration = 3600000;
}
