package com.kosta.sangsangseoga.global.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.UUID;

/**
 * SSE 연결(GET /api/notifications/stream) 전용 1회용 인증 티켓. 브라우저 EventSource는 커스텀 헤더를
 * 못 보내 쿼리 파라미터로 인증해야 하는데, 여기에 수명이 긴 JWT를 그대로 노출하면 로그/리퍼러/브라우저
 * 히스토리로 새어나갈 위험이 있다. 그래서 Authorization 헤더로 정상 인증한 뒤 짧은 TTL(30초)의 1회용
 * 티켓만 발급받아 쿼리 파라미터로 넘기고, 서버는 소비(조회 즉시 삭제)하는 방식으로 노출 범위를 최소화한다.
 */
@Component
@RequiredArgsConstructor
public class SseTicketService {

    private static final String KEY_PREFIX = "sse-ticket:";
    private static final Duration TICKET_TTL = Duration.ofSeconds(30);

    private final StringRedisTemplate redisTemplate;

    public String issue(Long memberId, String role) {
        String ticket = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(KEY_PREFIX + ticket, memberId + ":" + role, TICKET_TTL);
        return ticket;
    }

    /** 조회와 동시에 삭제(getAndDelete)해서 같은 티켓이 두 번 소비되지 않게 한다. */
    public Ticket consume(String ticket) {
        if (!StringUtils.hasText(ticket)) {
            return null;
        }
        String value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + ticket);
        if (value == null) {
            return null;
        }
        String[] parts = value.split(":", 2);
        if (parts.length != 2) {
            return null;
        }
        try {
            return new Ticket(Long.parseLong(parts[0]), parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Ticket {
        private final Long memberId;
        private final String role;
    }
}
