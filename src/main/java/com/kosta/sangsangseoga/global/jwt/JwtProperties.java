package com.kosta.sangsangseoga.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private static final String INSECURE_DEFAULT_SECRET = "defaultSecretKeySecretKeySecretKeySecretKeySecretKey";
    private static final int MIN_SECRET_LENGTH = 32;

    private String secretKey;
    private long accessTokenExpiration = 3600000;
    private long refreshTokenExpiration = 2592000000L;

    /**
     * JWT_SECRET_KEY가 설정되지 않았거나 소스에 노출된 기본값 그대로면 기동 자체를 막는다.
     * 이 값이 알려진 문자열이면 누구나 유효한 토큰을 위조할 수 있다.
     */
    @PostConstruct
    public void validate() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret-key(환경변수 JWT_SECRET_KEY)가 설정되지 않았습니다. " +
                            "고정 기본값으로 서버가 기동되지 않도록 반드시 값을 설정하세요."
            );
        }
        if (secretKey.equals(INSECURE_DEFAULT_SECRET)) {
            throw new IllegalStateException(
                    "jwt.secret-key가 소스코드에 노출된 기본값 그대로입니다. " +
                            "환경변수 JWT_SECRET_KEY를 실제 운영용 값으로 설정하세요."
            );
        }
        if (secretKey.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "jwt.secret-key 길이가 너무 짧습니다 (최소 " + MIN_SECRET_LENGTH + "자 이상 권장)."
            );
        }
    }
}
