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

    private static final int MIN_SECRET_LENGTH = 32;

    private String secretKey;
    private long accessTokenExpiration = 3600000;
    private long refreshTokenExpiration = 2592000000L;

    /**
     * prod 프로필은 application.yml에서 JWT_SECRET_KEY에 기본값을 주지 않으므로,
     * 환경변수가 없으면 Spring이 플레이스홀더를 못 찾아 이 빈이 생성되기 전에 이미 기동이 실패한다.
     * 여기서는 그 외에 값이 비어있거나(어떤 경로로든) 너무 짧은 경우만 방어한다.
     * dev 프로필의 편의용 기본 시크릿은 의도적으로 허용한다(로컬 전용이라 위험이 낮음).
     */
    @PostConstruct
    public void validate() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException(
                    "jwt.secret-key(환경변수 JWT_SECRET_KEY)가 설정되지 않았습니다."
            );
        }
        if (secretKey.length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException(
                    "jwt.secret-key 길이가 너무 짧습니다 (최소 " + MIN_SECRET_LENGTH + "자 이상 권장)."
            );
        }
    }
}
