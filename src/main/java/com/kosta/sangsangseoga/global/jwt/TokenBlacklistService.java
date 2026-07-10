package com.kosta.sangsangseoga.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * 관리자의 회원 정지/탈퇴, 보호자 동의 철회 등으로 회원 상태가 바뀐 시점을 회원 ID별로 Redis에 기록해,
 * 그 시점 이전에 발급된 access token은 만료 전이어도 더 이상 통과하지 못하게 한다.
 * JwtAuthFilter는 서명·만료만 검증하고 DB를 재조회하지 않으므로, 이 블랙리스트가 없으면 이미 발급된
 * access token은 정지/탈퇴 이후에도 자체 만료 시각(최대 1시간)까지 계속 유효하게 된다.
 */
@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String KEY_PREFIX = "token_invalid_after:";

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * cutoff 이전에 발급된 access token을 전부 무효로 취급한다.
     * TTL은 access token 최대 수명만큼만 유지한다 - 그 이후엔 어차피 모든 구 토큰이 자체 만료되므로
     * 이 기록을 계속 들고 있을 필요가 없다.
     */
    public void invalidateTokensIssuedBefore(Long memberId, Instant cutoff) {
        redisTemplate.opsForValue().set(
                key(memberId),
                String.valueOf(cutoff.toEpochMilli()),
                Duration.ofMillis(jwtProperties.getAccessTokenExpiration())
        );
    }

    public boolean isInvalidated(Long memberId, Date issuedAt) {
        String stored = redisTemplate.opsForValue().get(key(memberId));
        if (stored == null) {
            return false;
        }
        return issuedAt.getTime() <= Long.parseLong(stored);
    }

    private String key(Long memberId) {
        return KEY_PREFIX + memberId;
    }
}
