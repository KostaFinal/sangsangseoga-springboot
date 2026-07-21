# 전체 시스템 유스케이스 다이어그램

실제 컨트롤러 엔드포인트(auth / member / subscription / ai / book / myLibrary / friendLibrary / notification / admin)를 기준으로 정리했다. "설계 문서"가 아니라 "지금 코드가 실제로 제공하는 기능" 기준이라, `EditorController`/`ReadingController`처럼 클래스만 있고 메서드가 없는 빈 스텁은 유스케이스에 넣지 않았다.

## 액터

| 액터 | 설명 |
| --- | --- |
| 비회원 | 로그인하지 않은 사용자. 책 열람, 랭킹, 작가 검색 등 공개 API만 접근 가능 |
| 회원 (FREE / PREMIUM) | 로그인한 사용자. 플랜에 따라 AI 생성 사용량 한도가 다름([04-subscription.md](./04-subscription.md) 참고) |
| 관리자 | `/api/admin/**` 전용, `SecurityConfig`에서 `hasRole("ADMIN")`으로 제한 |
| (외부) AI 서버 | Python FastAPI + Gemini. 백엔드가 텍스트/이미지 생성을 위임 호출하는 대상 |
| (외부) OAuth 제공자 | 카카오 · 네이버. 소셜 로그인/가입에 연동 |

## 개요 — 액터와 도메인

```mermaid
flowchart LR
    Guest([비회원]):::guest
    Member([회원<br/>FREE / PREMIUM]):::member
    Admin([관리자]):::admin

    AiServer[["AI 서버<br/>FastAPI · Gemini"]]:::system
    OAuth[["OAuth 제공자<br/>카카오 · 네이버"]]:::system

    D1(("인증 · 계정"))
    D2(("구독 · 결제"))
    D3(("AI 생성"))
    D4(("책 만들기 · 감상"))
    D5(("내 서재"))
    D6(("커뮤니티<br/>(친구서재)"))
    D7(("알림"))
    D8(("운영 · 관리"))

    Guest --> D1
    Guest --> D4
    Guest --> D6
    Member --> D1
    Member --> D2
    Member --> D3
    Member --> D4
    Member --> D5
    Member --> D6
    Member --> D7
    Admin --> D8
    Admin --> D1

    D1 -.연동.-> OAuth
    D3 -.호출.-> AiServer

    classDef guest fill:transparent,stroke:#8A7A50,stroke-width:2px;
    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
    classDef admin fill:transparent,stroke:#A0473B,stroke-width:2px;
    classDef system fill:transparent,stroke-dasharray:4 3;
```

## 01. 인증 · 계정

`AuthController` · `OAuthController` · `MemberController`

```mermaid
flowchart LR
    Guest([비회원]):::guest
    Member([회원]):::member
    OAuth[["OAuth 제공자"]]:::system

    UC1(["회원가입"])
    UC2(["소셜 로그인/가입"])
    UC3(["로그인 / 로그아웃"])
    UC4(["토큰 재발급"])
    UC5(["비밀번호 재설정"])
    UC6(["보호자 동의 처리<br/>(만 14세 미만)"])
    UC7(["내 정보 조회 · 수정"])
    UC8(["회원 탈퇴"])

    Guest --> UC1
    Guest --> UC2
    Guest --> UC3
    Guest --> UC5
    UC2 -.연동.-> OAuth
    UC1 -.include.-> UC6

    Member --> UC4
    Member --> UC7
    Member --> UC8

    classDef guest fill:transparent,stroke:#8A7A50,stroke-width:2px;
    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
    classDef system fill:transparent,stroke-dasharray:4 3;
```

## 02. 구독 · 결제

`SubscriptionController` · `PaymentController` · `UsageController`

```mermaid
flowchart LR
    Member([회원]):::member

    UC1(["구독 플랜 조회"])
    UC2(["구독 시작<br/>FREE → PREMIUM"])
    UC3(["월간 → 연간 전환"])
    UC4(["해지 예약 / 재개"])
    UC5(["결제 내역 조회"])
    UC6(["오늘 사용량 조회"])

    Member --> UC1
    Member --> UC2
    Member --> UC3
    Member --> UC4
    Member --> UC5
    Member --> UC6

    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
```

## 03. AI 생성

`AiController` → `UsageService` · Python LLM 서버

```mermaid
flowchart LR
    Member([회원]):::member
    AiServer[["AI 서버"]]:::system

    UC1(["동화 텍스트 생성"])
    UC2(["삽화 이미지 생성"])
    UC3(["실시간 스트리밍 생성"])
    UC4(["사용량 확인 · 차감"])

    Member --> UC1
    Member --> UC2
    Member --> UC3
    UC1 -.include.-> UC4
    UC2 -.include.-> UC4
    UC1 -.호출.-> AiServer
    UC2 -.호출.-> AiServer

    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
    classDef system fill:transparent,stroke-dasharray:4 3;
```

## 04. 책 만들기 · 감상

`BookListController` · `WeeklyBookRankingController`

책 편집 자체는 프론트엔드에서 이루어지고, 백엔드는 `POST /api/books`(주석상 "책 생성(최종 저장)")로 발행 시점만 받는다. `EditorController`는 클래스만 있고 엔드포인트가 없는 빈 스텁이라 유스케이스에서 제외했다.

```mermaid
flowchart LR
    Guest([비회원]):::guest
    Member([회원]):::member

    UC1(["책 발행<br/>(최종 저장)"])
    UC2(["책 목록 · 상세 보기"])
    UC3(["조회수 기록"])
    UC4(["추천 도서 보기"])
    UC5(["주간 랭킹 · 신간 보기"])

    Guest --> UC2
    Guest --> UC5
    Member --> UC1
    Member --> UC2
    Member --> UC4
    UC2 -.include.-> UC3

    classDef guest fill:transparent,stroke:#8A7A50,stroke-width:2px;
    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
```

## 05. 내 서재

`MyLibraryController` · `BookReviewController` · `ReadingMemoController` · `ReadingPlanController`

```mermaid
flowchart LR
    Member([회원]):::member

    UC1(["위시리스트 관리"])
    UC2(["읽는 중 목록 · 진행률 갱신"])
    UC3(["완독 처리 / 재독"])
    UC4(["독후감 작성 · 수정"])
    UC5(["독후감 AI 피드백 받기"])
    UC6(["독서 메모 작성"])
    UC7(["독서 계획 등록 · 완료 처리"])
    UC8(["내 신고 내역 조회<br/>(신고한 것 / 받은 것)"])

    Member --> UC1
    Member --> UC2
    Member --> UC3
    Member --> UC4
    Member --> UC6
    Member --> UC7
    Member --> UC8
    UC4 -.extend.-> UC5

    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
```

## 06. 커뮤니티 (친구서재)

`BookLikeController` · `BookmarkController` · `CommentController` · `AuthorController` · `AuthorFollowController` · `ReportController`

```mermaid
flowchart LR
    Guest([비회원]):::guest
    Member([회원]):::member

    UC1(["책 좋아요 / 북마크"])
    UC2(["댓글 · 대댓글 작성"])
    UC3(["작가 목록 검색"])
    UC4(["작가 팔로우"])
    UC5(["부적절한 콘텐츠 신고"])
    UC6(["내 신고 목록 조회"])

    Guest --> UC3
    Member --> UC1
    Member --> UC2
    Member --> UC3
    Member --> UC4
    Member --> UC5
    Member --> UC6

    classDef guest fill:transparent,stroke:#8A7A50,stroke-width:2px;
    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
```

## 07. 알림

`NotificationController` (Redis Streams + SSE, [06-notification-realtime.md](./06-notification-realtime.md) 참고)

```mermaid
flowchart LR
    Member([회원]):::member

    UC1(["알림 목록 확인"])
    UC2(["알림 읽음 처리<br/>(단건 / 전체)"])
    UC3(["실시간 알림 수신<br/>(SSE 구독)"])

    Member --> UC1
    Member --> UC2
    Member --> UC3

    classDef member fill:transparent,stroke:#3B5BA0,stroke-width:2px;
```

## 08. 운영 · 관리

`AdminApi` / `AdminController` — 신고 처리 · 회원 관리 · AI 사용량 대시보드

```mermaid
flowchart LR
    Admin([관리자]):::admin

    UC1(["신고 처리"])
    UC2(["회원 목록 · 상태 관리"])
    UC3(["관리자 액션 로그 조회"])
    UC4(["AI 토큰 사용량<br/>추이 · 타임라인 조회"])

    Admin --> UC1
    Admin --> UC2
    Admin --> UC3
    Admin --> UC4

    classDef admin fill:transparent,stroke:#A0473B,stroke-width:2px;
```
