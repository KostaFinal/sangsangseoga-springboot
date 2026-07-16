# 알림 / 실시간 알림 (notification)

## 구조: DB(영구 이력) + Redis Streams(실시간 전달) + SSE(클라이언트 push)

알림은 두 경로로 동시에 나간다.

1. **DB 저장** — `Notification` 테이블에 항상 저장된다. `GET /api/notifications`로 조회하는 이력/뱃지 카운트의 유일한 정답(source of truth).
2. **실시간 push** — 같은 시점에 Redis Stream에 이벤트를 발행하고, 각 서버 인스턴스가 이를 구독해 해당 회원이 지금 연결해 둔 SSE 커넥션이 있으면 즉시 push한다.

Redis Stream은 **영구 이력이 아니라 순간 전달용**이라 `MAXLEN 1000`으로 짧게 트리밍한다. 연결이 없을 때 발생한 알림은 SSE로는 못 받지만 DB에는 남으므로, 다음에 `GET /api/notifications`를 부르면 그대로 보인다.

```mermaid
flowchart LR
    Service["도메인 서비스 (댓글/좋아요/팔로우 등)"] -->|notify(member, content)| NS[NotificationService]
    NS -->|"1) 즉시 저장"| DB[(notification 테이블)]
    NS -->|"2) 트랜잭션 커밋 후 발행"| Stream["Redis Stream (notification-stream)"]
    Stream -->|XREAD, Consumer Group 없음| Listener["NotificationStreamListener (서버 인스턴스별)"]
    Listener -->|"이 인스턴스에 연결된 회원인가?"| Registry[NotificationSseRegistry]
    Registry -->|SseEmitter.send| Client["클라이언트 (EventSource)"]
```

## 왜 이렇게 만들었나

- **지금은 서버가 1대라 사실 Redis 없이 로컬 메모리 push만으로도 충분하다**: 그럼에도 미리 넣은 이유는, 나중에 서버를 여러 대로 늘린 뒤에 이 계층을 추가하면 배포 중 일부 인스턴스만 적용된 과도기에 알림이 새는 구간이 생길 수 있어서, 처음부터 넣어 그 전환 리스크를 없앴다.
- **Redis Pub/Sub이 아니라 Streams**: Pub/Sub은 구독자가 없는 순간 메시지가 그냥 유실된다. Streams는 최소한 큐에 남는다(다만 이 구현은 `ReadOffset.latest()`로 시작해서 과거분 캐치업은 안 하고, 그 몫은 DB 조회가 담당한다).
- **Consumer Group을 안 쓴다**: Consumer Group은 메시지를 그룹 내 인스턴스 하나에게만 배분한다. 그런데 하필 그 인스턴스에 대상 회원의 SSE 연결이 없으면 메시지가 유실된다. 그래서 모든 인스턴스가 `XREAD`로 전체 이벤트를 다 받고, 각자 로컬 레지스트리로 "내가 이 회원과 연결되어 있는가"만 필터링한다.
- **SSE (WebSocket/STOMP 아님)**: 알림은 서버→클라이언트 단방향이라 양방향 채널이 필요 없다. 이미 AI 텍스트 스트리밍(`AiStreamService`)에서 `SseEmitter`를 쓰고 있어서 같은 인프라를 재사용했다.
- **알림 발행용 Redis 커넥션 분리**: 스트림 리스너는 `XREAD`를 2초 poll timeout으로 블로킹하며 계속 반복한다. 앱의 기본 Redis 커넥션(토큰 블랙리스트 등과 공유)과 같은 커넥션을 쓰면 안 되므로 `NotificationStreamRedisConfig`에서 전용 `LettuceConnectionFactory`를 따로 둔다.
- **트랜잭션 커밋 후 발행**: Redis 발행은 롤백이 안 된다. 신고 처리 같은 트랜잭션이 롤백되면 알림 자체가 없던 일이 되어야 하므로, `AfterCommitTask` 이벤트로 커밋 이후에만 Redis에 발행한다(DB row는 트랜잭션 안에서 정상적으로 같이 롤백된다).
- **SSE 연결 직후 더미 이벤트 전송**: 진짜 알림이 오기 전까지 아무것도 안 보내면 Tomcat이 응답 헤더 자체를 flush하지 않아 클라이언트가 연결 성공 여부를 알 수 없다. `NotificationSseRegistry.register()`에서 연결 직후 `:connected` 코멘트 이벤트를 바로 보내 헤더를 강제로 내보낸다.

## SSE 엔드포인트

```
GET /api/notifications/stream
```

- 이벤트명 `notification`, payload는 `GET /api/notifications` 응답 아이템과 동일한 shape(`id`, `text`, `createdAt`, `read`).
- 브라우저 `EventSource`는 커스텀 헤더를 못 보내므로, **이 경로에 한해서만** `Authorization` 헤더 대신 `?token=` 쿼리 파라미터로 인증한다(`JwtAuthFilter.resolveToken`에서 경로를 명시적으로 예외 처리).
- 회원별 연결은 `NotificationSseRegistry`가 서버 인스턴스 로컬 메모리(`Map<Long, List<SseEmitter>>`)로 들고 있다. 탭마다 별도 연결이 붙고(`CopyOnWriteArrayList`), 타임아웃은 30분(무한 아님) — `EventSource`가 만료 시 알아서 재연결하게 하기 위함.

## 알림이 발생하는 지점 전체 목록

| 트리거 | 알림 받는 사람 | 비고 |
| --- | --- | --- |
| 신간 등록 | 작가를 팔로우하는 회원 전원 | 최초부터 있던 기능 |
| 책 신고 접수 | 신고당한 책의 작가 | 최초부터 있던 기능 |
| 작가 팔로우 | 팔로우당한 작가 | 본인 팔로우는 애초에 막혀 있음(자기 자신 불가) |
| 책에 댓글 작성 | 책 작가 | 본인 책이면 알림 안 감 |
| 댓글에 답글 작성 | 원댓글 작성자 | 본인 댓글이면 알림 안 감 |
| 책 좋아요 | 책 작가 | 본인 책이면 알림 안 감 |
| 관리자의 신고 처리(승인/반려) | 신고한 사람 | `AdminService.processReport` |
| 관리자의 신고 조치(책 숨김/댓글 삭제/작가 정지) | 조치당한 콘텐츠 소유자 | `AdminService.applyAction` |
| 관리자의 회원 상태 변경(정지/탈퇴/복구) | 해당 회원 | 사유(`reason`)도 같이 전달 |
| 보호자 동의 승인/거절/철회 | 자녀 회원 | 계정 상태(PENDING↔ACTIVE)가 걸린 결정이라 특히 중요 |
| 구독 자동갱신 | 해당 회원 | `SubscriptionService.reconcileIfExpired` |
| 구독 만료로 FREE 다운그레이드 | 해당 회원 | 위와 같은 진입점, 스케줄러와 API가 공유 |

의도적으로 제외한 것: **책 리뷰(`BookReview`) 작성**은 다른 회원이나 작가에게 공개되지 않는 개인 비공개 독서 기록이라 알림 대상에서 뺐다.

## 관련 파일

- `domain/notification/realtime/NotificationSseRegistry.java` — 인스턴스 로컬 SSE 연결 레지스트리
- `domain/notification/realtime/NotificationStreamPublisher.java` — Redis Stream 발행(`XADD` + 트리밍)
- `domain/notification/realtime/NotificationStreamListener.java` — Consumer Group 없는 `XREAD` 구독, 로컬 레지스트리로 push
- `domain/notification/service/NotificationServiceImpl.java` — DB 저장 + 커밋 후 Redis 발행(`notify()`)
- `domain/notification/controller/NotificationController.java` — REST API + SSE 구독 엔드포인트
- `global/config/NotificationStreamRedisConfig.java` — 스트림 리스너 전용 Redis 커넥션
- `global/jwt/JwtAuthFilter.java` — SSE 경로 전용 `?token=` 인증 예외
- `global/event/AfterCommitTask.java` — 트랜잭션 커밋 후 실행 이벤트(Redis 발행에 재사용)
