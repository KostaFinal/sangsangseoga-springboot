package com.kosta.sangsangseoga.domain.notification.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 알림 실시간 전달용 Redis Stream 발행. 이 스트림은 "지금 연결된 클라이언트에게 바로 알려주는" 용도일
 * 뿐이라 길이를 짧게 유지한다 - 알림의 영구 이력은 notification 테이블(DB)이 담당한다.
 */
@Component
@RequiredArgsConstructor
public class NotificationStreamPublisher {

    public static final String STREAM_KEY = "notification-stream";
    private static final long STREAM_MAX_LEN = 1000L;

    private final StringRedisTemplate redisTemplate;

    public void publish(Long notificationId, Long memberId, String content, LocalDateTime createdAt) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("notificationId", String.valueOf(notificationId));
        fields.put("memberId", String.valueOf(memberId));
        fields.put("content", content);
        fields.put("createdAt", createdAt.toString());

        redisTemplate.opsForStream().add(StreamRecords.newRecord()
                .ofMap(fields)
                .withStreamKey(STREAM_KEY));
        redisTemplate.opsForStream().trim(STREAM_KEY, STREAM_MAX_LEN);
    }
}
