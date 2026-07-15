package com.kosta.sangsangseoga.domain.notification.realtime;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 서버 인스턴스별로 "지금 이 회원이 SSE로 연결되어 있는지"를 들고 있는 로컬(메모리) 레지스트리.
 * 여러 서버가 떠 있으면 각자 자기한테 붙어있는 연결만 안다 - Redis Stream 리스너가 이 레지스트리를
 * 조회해서 자기 서버에 연결된 회원에게만 실제로 push한다.
 */
@Component
public class NotificationSseRegistry {

    private static final long EMITTER_TIMEOUT_MILLIS = 30 * 60 * 1000L; // 30분. 만료되면 EventSource가 알아서 재연결한다.

    private final Map<Long, List<SseEmitter>> emittersByMemberId = new ConcurrentHashMap<>();

    public SseEmitter register(Long memberId) {
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT_MILLIS);
        emittersByMemberId.computeIfAbsent(memberId, id -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> remove(memberId, emitter));
        emitter.onTimeout(() -> remove(memberId, emitter));
        emitter.onError(e -> remove(memberId, emitter));

        return emitter;
    }

    public void sendTo(Long memberId, String eventName, Object payload) {
        List<SseEmitter> emitters = emittersByMemberId.get(memberId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name(eventName).data(payload));
            } catch (IOException | IllegalStateException e) {
                remove(memberId, emitter);
            }
        }
    }

    private void remove(Long memberId, SseEmitter emitter) {
        List<SseEmitter> emitters = emittersByMemberId.get(memberId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByMemberId.remove(memberId);
        }
    }
}
