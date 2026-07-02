package com.kosta.sangsangseoga.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

/**
 * 비밀번호 재설정, 보호자 동의 등 이메일 링크에 담기는 1회성 목적 한정(purpose-scoped) 토큰 발급/검증.
 * DB에 별도 토큰 컬럼을 두지 않고, 서명된 JWT 자체로 위변조·만료 여부를 검증한다.
 * 로그인 access/refresh 토큰(JwtTokenProvider)과는 별개로 관리한다.
 */
@Component
@RequiredArgsConstructor
public class ActionTokenProvider {

    private final JwtProperties jwtProperties;

    public String create(String purpose, Long subjectId, long ttlMillis) {
        return create(purpose, subjectId, ttlMillis, Map.of());
    }

    public String create(String purpose, Long subjectId, long ttlMillis, Map<String, String> extraClaims) {
        JWTCreator.Builder builder = JWT.create()
                .withSubject(String.valueOf(subjectId))
                .withClaim("purpose", purpose)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(ttlMillis)));
        extraClaims.forEach(builder::withClaim);
        return builder.sign(Algorithm.HMAC256(jwtProperties.getSecretKey()));
    }

    public DecodedJWT verify(String token, String purpose) {
        try {
            return JWT.require(Algorithm.HMAC256(jwtProperties.getSecretKey()))
                    .withClaim("purpose", purpose)
                    .build()
                    .verify(token);
        } catch (TokenExpiredException e) {
            throw new ActionTokenExpiredException();
        } catch (JWTVerificationException e) {
            throw new ActionTokenInvalidException();
        }
    }

    public Long getSubjectId(DecodedJWT decodedJWT) {
        return Long.valueOf(decodedJWT.getSubject());
    }
}
