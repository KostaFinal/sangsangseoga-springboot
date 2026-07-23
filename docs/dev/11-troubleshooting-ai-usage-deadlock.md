# 트러블슈팅: AI 생성 시 MariaDB Deadlock(1213) / Lock wait timeout(1205)

## 증상

- AI 텍스트/이미지 생성(`AiService.generate()`, `AiImageService.generateImage()`) 중 간헐적으로 발생.
- 트래픽이 적을 땐 안 나고, 동시 요청이 몰리는 시간대에 발생.
- Spring Boot 서버를 재시작해도 해결 안 됨 (코드 로직 문제라 재시작 후 트래픽이 다시 들어오면 바로 재현).

## 한 줄 요약

AI 응답을 기다리는 동안 회원 정보(member row)에 잠금을 걸어놓고 있었다. 그 잠긴 상태에서 "사용 기록 남기기"와 "사용권 차감하기"의 실행 순서가 꼬여 있어서, 같은 회원이 AI를 거의 동시에 두 번 요청하면 두 요청이 서로를 기다리다 꽉 막히거나(데드락), 잠금이 오래 풀리지 않아 다른 작업까지 대기하다 타임아웃 났다.

## 원인 1: DB 트랜잭션이 Python(FastAPI) 호출까지 감싸고 있었음

`AiService.generate()`는 통째로 하나의 트랜잭션(`@Transactional`)이었고, 그 안에 다음이 순서대로 들어 있었다.

1. 쿼터 확인 (`assertCanGenerateText`) — 여기서 이미 member row에 락을 거는 UPDATE가 한 번 실행됨
2. **FastAPI 동기 호출** (Gemini 응답 대기, 타임아웃 없음 = 무한정 대기 가능)
3. 사용 기록 저장 (`recordUsage`)
4. 사용권 차감 (`consumeText`)

1번에서 이미 DB를 건드렸기 때문에, 2번에서 Python 응답을 기다리는 동안에도 트랜잭션은 끝나지 않고 **member row 락과 DB 커넥션을 계속 붙잡고 있었다.** Python이 느리게 응답할수록 이 잠긴 시간이 길어지고, 그 사이 같은 회원을 건드리는 다른 요청(구독 변경, 책 발행 등)은 잠금이 풀리길 기다리다가 50초(MariaDB 기본 대기 한도)를 넘기면 **Lock wait timeout(1205)** 으로 실패했다.

## 원인 2: DB 작업 순서가 뒤바뀌어 있었음

```java
recordUsage(memberId, ...);         // ai_generation_usage 테이블에 새 행 INSERT
usageService.consumeText(memberId); // member 테이블 UPDATE (사용권 차감)
```

`ai_generation_usage`는 어느 회원의 기록인지 `member_id`로 연결돼 있는 자식 테이블이다. DB는 자식 행을 넣을 때 "이 회원이 실제로 존재하는지" 확인하려고 부모(member) 행에 **가벼운 잠금(공유 락)** 을 건다. 그 다음 줄인 `consumeText`는 같은 회원 행에 **강한 잠금(배타 락)** 을 요구한다.

같은 회원이 AI 생성을 동시에 두 번 요청하면 이렇게 된다.

```
요청A: 기록 INSERT → 회원 행에 공유 락 획득
요청B: 기록 INSERT → 회원 행에 공유 락 획득 (공유 락끼리는 같이 걸 수 있어서 둘 다 성공)
요청A: 차감 UPDATE → 강한 잠금 필요 → 요청B가 쥔 공유 락 때문에 대기
요청B: 차감 UPDATE → 강한 잠금 필요 → 요청A가 쥔 공유 락 때문에 대기
→ 서로가 서로를 기다림 → MariaDB가 둘 중 하나를 강제로 취소: Deadlock (1213)
```

기록(INSERT)을 먼저 하고 차감(UPDATE)을 나중에 한 순서가 문제였다. 순서를 바꿔서 차감 먼저, 기록 나중에 하면 이 충돌이 안 생긴다. (실제로 `SubscriptionService`의 결제 처리 코드는 이미 이 순서 — 회원 정보 먼저 갱신, 결제 기록은 나중 — 를 지키고 있었다. AI 쪽만 반대로 되어 있었다.)

## 원인 3: 이미지 생성 쪽도 같은 패턴

`AiImageService`도 기록 저장 → 사용권 차감 순서가 똑같이 뒤바뀌어 있었다. 다만 이쪽은 트랜잭션이 걸려있지 않아서(주석 처리됨) 데드락 확률은 낮았지만, 대신 "기록만 되고 차감은 안 되는" 식의 불일치가 날 수 있는 구조였다.

## 해결

1. **차감과 기록을 하나로 묶고 순서를 고정**: `UsageService`에 `consumeTextAndRecordUsage()` / `consumeImageAndRecordUsage()`를 새로 만들어, 한 트랜잭션 안에서 **차감(UPDATE)을 먼저, 기록(INSERT)을 나중에** 실행하도록 고정했다. 기존에 따로 있던 차감 메서드와 기록 메서드는 삭제.
2. **트랜잭션 범위를 DB 작업만으로 축소**: `AiService`, `AiImageService`에서 클래스 전체를 감싸던 `@Transactional`을 없앴다. 이제 Python 호출은 트랜잭션 밖에서 실행되고, DB 쓰기가 필요한 순간(쿼터 확인, 차감+기록)만 각각 짧게 트랜잭션을 열었다 닫는다. Python 응답을 기다리는 동안 더 이상 락을 붙잡고 있지 않는다.
3. **타임아웃 추가**: FastAPI 호출에 연결 5초 / 응답 최대 2분(`application.yml`의 `fastapi.connect-timeout-ms`, `fastapi.read-timeout-ms`) 제한을 걸었다. FastAPI가 멈춰도 무한정 대기하는 대신 일정 시간 후 에러로 종료된다.

## 변경 파일

- `src/main/java/com/kosta/sangsangseoga/domain/subscription/service/UsageService.java`
- `src/main/java/com/kosta/sangsangseoga/domain/ai/service/AiService.java`
- `src/main/java/com/kosta/sangsangseoga/domain/ai/service/AiImageService.java`
- `src/main/resources/application.yml`

## 참고

- `BookService.publish()` → `UsageService.markFreeTrialUsed()` 경로(`MemberOptimisticRetrySupport`, 낙관적 락 재시도)는 원래도 구조적 문제가 없어 손대지 않았다. `AiService`가 더 이상 Python 호출 동안 락을 오래 쥐지 않게 되면서, 이 경로가 그 락에 걸려 타임아웃 날 가능성도 같이 줄었다.
- 왜 오전엔 괜찮다가 오후에 터졌는지: 데드락/락 대기는 두 요청이 정확히 겹치는 타이밍이 맞아야 재현되는 확률적 문제다. 트래픽이 적으면 안 겹쳐서 안 보이고, 동시 요청이 늘어나는 시간대에 겹칠 확률이 올라가면서 처음으로 드러난 것뿐이다. 원래부터 있던 결함이었다.
