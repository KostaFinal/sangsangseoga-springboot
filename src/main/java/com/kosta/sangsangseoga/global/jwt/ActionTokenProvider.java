package com.kosta.sangsangseoga.global.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 비밀번호 재설정, 보호자 동의 등 이메일 링크에 담기는 1회성 목적 한정(purpose-scoped) 토큰 발급/검증.
 * DB에 별도 토큰 컬럼을 두지 않고, 서명된 JWT 자체로 위변조·만료 여부를 검증한다.
 * 로그인 access/refresh 토큰(JwtTokenProvider)과는 별개로 관리한다.
 */
@Component
@RequiredArgsConstructor
public class ActionTokenProvider {

    private static final String CLAIM_PURPOSE = "purpose";
    private static final Set<String> RESERVED_CLAIMS = Set.of("sub", "purpose", "jti", "iat", "exp");
    private static final String CONSUMED_KEY_PREFIX = "action_token_used:";

    private final JwtProperties jwtProperties;
    private final StringRedisTemplate redisTemplate;

    public String create(String purpose, Long subjectId, long ttlMillis) {
        return create(purpose, subjectId, ttlMillis, Map.of());
    }

    public String create(String purpose, Long subjectId, long ttlMillis, Map<String, String> extraClaims) {
        for (String key : extraClaims.keySet()) {
            if (RESERVED_CLAIMS.contains(key)) {
                throw new IllegalArgumentException("예약된 클레임 이름은 extraClaims에 사용할 수 없습니다: " + key);
            }
        }
        JWTCreator.Builder builder = JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withSubject(String.valueOf(subjectId))
                .withClaim(CLAIM_PURPOSE, purpose)
                .withIssuedAt(new Date())
                .withExpiresAt(Date.from(Instant.now().plusMillis(ttlMillis)));
        extraClaims.forEach(builder::withClaim);
        return builder.sign(Algorithm.HMAC256(jwtProperties.getSecretKey()));
    }

    public DecodedJWT verify(String token, String purpose) {
        try {
            return JWT.require(Algorithm.HMAC256(jwtProperties.getSecretKey()))
                    .withClaim(CLAIM_PURPOSE, purpose)
                    .build()
                    .verify(token);
        } catch (TokenExpiredException e) {
            throw new ActionTokenExpiredException();
        } catch (JWTVerificationException e) {
            throw new ActionTokenInvalidException();
        }
    }

    /**
     * 토큰을 1회 소비 처리한다. verify() 성공 직후, 실제 민감한 액션(비밀번호 변경/동의 승인)을
     * 수행하기 직전에 호출해야 한다. Redis SETNX로 원자적으로 처리해 동시 요청에서도 한 번만 성공하며,
     * 이미 소비된(또는 동시에 먼저 소비된) 토큰이면 ActionTokenInvalidException을 던진다.
     */
    public void consume(DecodedJWT decodedJWT) {
        String key = CONSUMED_KEY_PREFIX + decodedJWT.getId();
        long ttlSeconds = Math.max(1, Duration.between(Instant.now(), decodedJWT.getExpiresAt().toInstant()).getSeconds());
        Boolean firstUse = redisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofSeconds(ttlSeconds));
        if (Boolean.FALSE.equals(firstUse)) {
            throw new ActionTokenInvalidException();
        }
    }

    public Long getSubjectId(DecodedJWT decodedJWT) {
        return Long.valueOf(decodedJWT.getSubject());
    }
}
