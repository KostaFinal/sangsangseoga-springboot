package com.kosta.sangsangseoga.domain.notification.realtime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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
 *
 * CloudFront가 EC2 origin으로 프록시할 때 origin 응답 타임아웃(기본 30초) 동안 아무 데이터도
 * 안 흐르면 연결을 강제로 끊는다 - 실제 알림이 뜸하면 이 상태가 30초 넘게 지속돼 클라이언트가
 * ERR_HTTP2_PROTOCOL_ERROR로 끊기는 걸 실제로 겪었다. sendHeartbeat가 그 타임아웃보다 짧은
 * 주기로 빈 핑을 흘려보내 연결을 계속 "살아있는" 상태로 유지한다.
 */
@Slf4j
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

        // 실제 알림이 올 때까지 아무것도 안 보내면 서버가 응답 헤더 자체를 flush하지 않아 클라이언트가
        // 연결 성공 여부를 알 수 없다. 연결 직후 바로 코멘트 이벤트를 보내 헤더를 강제로 내보낸다.
        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            remove(memberId, emitter);
        }

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

    /**
     * CloudFront origin 응답 타임아웃(기본 30초)보다 짧은 주기로 연결된 모든 emitter에 빈 핑을
     * 보낸다. 실제 알림 이벤트가 아니라 코멘트 프레임이라 EventSource의 onmessage에는 안 잡히고,
     * 그냥 "이 연결 아직 살아있음"을 CloudFront/브라우저 양쪽에 알리는 용도다.
     */
    @Scheduled(fixedRate = 15_000L)
    public void sendHeartbeat() {
        for (Map.Entry<Long, List<SseEmitter>> entry : emittersByMemberId.entrySet()) {
            Long memberId = entry.getKey();
            for (SseEmitter emitter : entry.getValue()) {
                try {
                    emitter.send(SseEmitter.event().comment("heartbeat"));
                } catch (IOException | IllegalStateException e) {
                    remove(memberId, emitter);
                }
            }
        }
    }

    /**
     * 맵 항목 삭제 여부 판단(비었는지 체크)과 실제 삭제를 computeIfPresent 하나로 묶어 원자적으로 처리한다.
     * 분리되어 있으면 "비어서 삭제하려는 시점"과 "동시에 register()가 새 emitter를 추가하는 시점"이 겹칠 때
     * 방금 등록된 emitter까지 통째로 유실될 수 있다.
     */
    private void remove(Long memberId, SseEmitter emitter) {
        emittersByMemberId.computeIfPresent(memberId, (id, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
