package com.kosta.sangsangseoga.domain.notification.realtime;

import com.kosta.sangsangseoga.domain.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Redis Stream을 Consumer Group 없이 각 서버 인스턴스가 독립적으로 구독한다. Consumer Group을 쓰면
 * 메시지가 그룹 내 한 인스턴스에게만 배분되는데, 하필 그 인스턴스에 대상 회원의 SSE 연결이 없으면
 * 메시지가 그냥 유실된다. 대신 모든 인스턴스가 전체 이벤트를 다 보고, 자기 로컬 레지스트리
 * (NotificationSseRegistry)에 그 회원의 연결이 있을 때만 실제로 push한다.
 *
 * ReadOffset.latest()로 시작하므로 리스너가 뜬 시점 이후의 이벤트만 받는다(과거분 캐치업 없음).
 * 놓친 알림은 GET /api/notifications(DB 조회)로 커버되므로 문제 없다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationStreamListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final NotificationSseRegistry sseRegistry;
    private final RedisConnectionFactory redisConnectionFactory;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @PostConstruct
    void start() {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build();

        container = StreamMessageListenerContainer.create(redisConnectionFactory, options);
        container.receive(StreamOffset.create(NotificationStreamPublisher.STREAM_KEY, ReadOffset.latest()), this);
        container.start();
    }

    @PreDestroy
    void stop() {
        if (container != null) {
            container.stop();
        }
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        Map<String, String> fields = message.getValue();
        try {
            Long memberId = Long.valueOf(fields.get("memberId"));
            NotificationDto payload = NotificationDto.builder()
                    .id(Long.valueOf(fields.get("notificationId")))
                    .text(fields.get("content"))
                    .createdAt(LocalDateTime.parse(fields.get("createdAt")))
                    .read(false)
                    .build();
            sseRegistry.sendTo(memberId, "notification", payload);
        } catch (Exception e) {
            log.warn("알림 스트림 메시지 처리 실패: {}", fields, e);
        }
    }
}
