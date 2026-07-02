package com.kosta.sangsangseoga.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 회원 ID를 키로 Refresh Token을 Redis에 화이트리스트 방식으로 관리한다.
 * 회원당 최신 Refresh Token 1개만 유효하며(다중 기기 동시 로그인 시 이전 토큰은 자동 무효화됨),
 * 로그아웃 시 즉시 삭제한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String KEY_PREFIX = "refresh_token:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    public void save(Long memberId, String refreshToken) {
        redisTemplate.opsForValue().set(
                key(memberId),
                refreshToken,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())
        );
    }

    public boolean matches(Long memberId, String refreshToken) {
        String stored = redisTemplate.opsForValue().get(key(memberId));
        return stored != null && stored.equals(refreshToken);
    }

    public void delete(Long memberId) {
        redisTemplate.delete(key(memberId));
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}
