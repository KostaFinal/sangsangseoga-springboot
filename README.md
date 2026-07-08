# 상상서가 (SangSangSeoGa) - Backend

AI를 활용해 누구나 자신만의 동화, 에세이, 시, 소설을 만들고 공유할 수 있는 창작·독서 플랫폼의 백엔드 프로젝트입니다.

## 기술 스택

- Java 11 / Spring Boot 2.7.17
- Spring Data JPA, QueryDSL 5.0.0
- Spring Security + JWT (com.auth0:java-jwt)
- MariaDB
- Redis
- Gemini API (텍스트 생성)
- springdoc-openapi (Swagger UI)

## ERD

[dbdiagram.io - Kosta Final SangSangSeoGa](https://dbdiagram.io/d/Kosta-Final-SangSangSeoGa-6a39f4e19340ecc065f33680)

## 도메인 구조

| 도메인 | 설명 |
| --- | --- |
| `auth` | 회원가입, 로그인/로그아웃, 토큰 재발급, 비밀번호 재설정 |
| `member` | 회원 정보 조회/수정, 보호자(법정대리인) 동의, 회원 탈퇴 |
| `book` | 책, 책 페이지, 책 이미지, 태그, 주간 인기 랭킹 |
| `editor` | 장르별(에세이/동화/소설/시/비문학) AI 창작 설정값 모델링 |
| `ai` | AI 텍스트·이미지 생성 호출 및 토큰/생성량 사용 기록 |
| `subscription` | 구독 플랜, 결제, 사용량 조회 |
| `myLibrary` | 내 서재 - 독서 상태, 독서 메모, 독후감, 독서 계획 |
| `friendLibrary` | 친구 서재 - 좋아요, 북마크, 작가 팔로우, 댓글, 신고 |
| `admin` | 신고 처리 등 관리자 액션 로그 |

## 프로젝트 구조

각 도메인(`domain/{도메인명}`)은 아래와 같은 레이어드 구조를 따릅니다. 기능을 찾을 때 이 순서로 따라가면 됩니다.

```
domain/{도메인명}
 ├─ controller   # HTTP 요청/응답 (엔드포인트)
 ├─ service      # 비즈니스 로직
 ├─ repository   # DB 접근 (Spring Data JPA / QueryDSL)
 ├─ entity       # DB 테이블과 매핑되는 JPA 엔티티
 ├─ dto          # 요청/응답에 사용하는 데이터 객체
 └─ enums        # 도메인에서 쓰는 상태값(enum)
```

## 공통 응답 형식

모든 API는 `ApiResponse<T>` (`global/common/ApiResponse.java`)로 감싸서 응답합니다.

```json
{
  "success": true,
  "data": { "...": "실제 응답 데이터" },
  "code": null,
  "message": "성공"
}
```

- `success`: 요청 성공 여부
- `data`: 성공 시 실제 데이터, 실패 시 `null`
- `code`: 비즈니스 에러 식별 코드 (성공 시 `null`)
- `message`: 사람이 읽을 수 있는 메시지 (디버깅용)

## 시작하기

### 1. 사전 준비물

아래 프로그램이 설치되어 있어야 합니다. Gradle은 wrapper(`gradlew`)가 포함되어 있어 따로 설치할 필요 없습니다.

- JDK 11 이상 (`java -version`으로 확인)
- MariaDB (기본 포트 3306)
- Docker Desktop (Redis, MailHog 컨테이너 실행용) - 설치 후 **실행되어 있는 상태**여야 합니다.
- Git

### 2. 저장소 클론

```bash
git clone <저장소 주소>
cd sangsangseoga-springboot
```

**IntelliJ**: `File → Open`으로 프로젝트 루트(`build.gradle`이 있는 폴더)를 열면 Gradle 프로젝트로 자동 인식됩니다.

**STS(Spring Tool Suite)**: `File → Import → Gradle → Existing Gradle Project`를 선택한 뒤 프로젝트 루트를 지정하고 Finish. Import 후 프로젝트 우클릭 → `Gradle → Refresh Gradle Project`를 한 번 해주면 의존성이 정상적으로 잡힙니다.

### 3. 데이터베이스 스키마 생성

MariaDB에 접속해서 빈 스키마만 만들어두면, 테이블은 서버가 자동으로 생성합니다.

```sql
CREATE DATABASE sangsangseoga CHARACTER SET utf8mb4;
```

### 4. 환경 변수 설정

`src/main/resources/application.yml`의 `dev` 프로필 기준입니다. 대부분 기본값이 있어 로컬 개발 시 없어도 실행되지만, 필요하면 아래처럼 재정의할 수 있습니다.

| 변수명 | 기본값(dev) | 설명 |
| --- | --- | --- |
| `DB_USERNAME` | `root` | MariaDB 계정 |
| `DB_PASSWORD` | `application.yml` 참고 | MariaDB 비밀번호 |
| `REDIS_HOST` | `localhost` | Redis 호스트 |
| `REDIS_PORT` | `6379` | Redis 포트 |
| `MAIL_HOST` | `localhost` | SMTP 호스트. 로컬은 docker-compose의 MailHog(1025)로 발송 |
| `MAIL_PORT` | `1025` | SMTP 포트 (MailHog 기본 포트) |
| `MAIL_FROM` | `no-reply@sangsangseoga.local` | 발신자 이메일 주소 |
| `JWT_SECRET_KEY` | 기본 문자열 제공 | JWT 서명 키 |
| `GEMINI_API_KEY` | 없음 (**필수**) | Gemini API 키. 없으면 AI 생성 기능만 실패하고 나머지 서버 구동에는 문제 없음 |

환경 변수는 아래 중 편한 방법으로 설정하면 됩니다.

**IntelliJ 사용 시**: Run/Debug Configurations → Environment variables 항목에 `GEMINI_API_KEY=발급받은키` 형식으로 입력

**STS(Spring Tool Suite) 사용 시**: 상단 메뉴 Run → Run Configurations → 실행할 Spring Boot App 선택 → `Environment` 탭 → `New` 버튼으로 `Name: GEMINI_API_KEY`, `Value: 발급받은키` 입력 후 Apply → Run

**터미널(Mac/Linux) 사용 시**
```bash
export GEMINI_API_KEY=발급받은키
```

**터미널(Windows PowerShell) 사용 시**
```powershell
$env:GEMINI_API_KEY="발급받은키"
```

운영(`prod`) 프로필은 `PROD_DB_HOST`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD`, `PROD_REDIS_HOST`, `JWT_SECRET_KEY`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`을 반드시 환경변수로 주입해야 서버가 뜹니다(기본값 없음).

### 5. Redis / MailHog 컨테이너 실행 (Docker)

이 프로젝트는 Redis와 메일 발송 테스트용 MailHog를 `docker-compose.yml`로 띄우는 것을 기준으로 합니다.

**Docker Desktop이 설치되어 있고 실행 중이어야 합니다.** (트레이 아이콘이 떠 있고 "Docker Desktop is running" 상태인지 확인)

로컬에 Redis를 직접 설치해서 이미 켜둔 적이 있다면(예: `redis-server`를 서비스로 등록했거나 백그라운드로 띄워둔 경우), 포트 `6379`가 중복되어 컨테이너가 뜨지 않거나 엉뚱한 Redis에 연결될 수 있습니다. Docker로 실행하기 전에 로컬 Redis를 먼저 꺼주세요.

```powershell
# Windows: 6379 포트를 쓰는 프로세스 확인 후 종료
netstat -ano | findstr :6379
taskkill /PID <위에서 확인한 PID> /F

# Redis를 서비스로 설치했다면
net stop Redis
```

```bash
# Mac/Linux
sudo systemctl stop redis      # systemd로 설치한 경우
brew services stop redis       # Homebrew로 설치한 경우
```

로컬 Redis를 정리했다면 프로젝트 루트에서 컨테이너를 띄웁니다.

```bash
docker compose up -d
```

정상적으로 뜨면 `docker ps`에서 `sangsangseoga-redis`(6379), `sangsangseoga-mailhog`(1025, 8025)가 보입니다. MailHog 웹 UI는 `http://localhost:8025`에서 확인할 수 있습니다.

컨테이너를 내리려면:

```bash
docker compose down
```

### 6. 서버 최초 실행 (테이블 자동 생성)

`spring.jpa.hibernate.ddl-auto=update` 설정 덕분에, 서버를 한 번 기동하면 엔티티 기준으로 테이블이 자동 생성됩니다. 별도의 DDL 스크립트를 실행할 필요가 없습니다.

```bash
# Mac/Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

**IntelliJ**: 메인 클래스(`SangsangseogaApplication`) 우클릭 → `Run`으로도 실행 가능합니다.

**STS**: 프로젝트 우클릭 → `Run As → Spring Boot App`으로 실행합니다. (하단 `Boot Dashboard` 뷰에서도 프로젝트를 선택해 Start/Restart 가능)

콘솔에 `Started SangsangseogaApplication` 같은 로그가 뜨고 에러 없이 실행되면, MariaDB에 테이블이 전부 생성된 것입니다. 확인 후 `Ctrl+C`(또는 STS의 정지 버튼)로 서버를 종료합니다.

### 7. 더미 데이터 삽입

테이블 생성이 끝난 뒤 `src/main/resources/docs/sql/dummy_data.sql`을 실행합니다. 테이블 간 참조(FK) 순서에 맞춰 작성되어 있으므로 **전체를 한 번에 실행**해야 합니다.

```bash
mysql -u root -p sangsangseoga < src/main/resources/docs/sql/dummy_data.sql
```

(GUI 툴을 쓴다면 DBeaver, HeidiSQL 등에서 해당 파일을 열어 전체 실행해도 됩니다.)

### 8. 서버 재실행

```bash
./gradlew bootRun        # Mac/Linux
gradlew.bat bootRun       # Windows
```

IntelliJ에서는 Run 버튼, STS에서는 `Run As → Spring Boot App` 또는 Boot Dashboard의 Restart로 동일하게 재실행할 수 있습니다.

## API 문서 (Swagger)

서버 실행 후 아래 주소에서 전체 API를 확인/테스트할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

로그인(`/api/auth/login`)으로 발급받은 Access Token을 우측 상단 `Authorize` 버튼에 입력하면, 이후 모든 요청에 `Authorization: Bearer <token>` 헤더가 자동으로 포함됩니다.

## 테스트 계정

`/api/admin/**` 등 ADMIN 권한이 필요한 API를 테스트할 때 쓸 수 있는 계정입니다.

| 이메일 | 비밀번호 | 권한 | 비고 |
|---|---|---|---|
| `admintest2@example.com` | `test1234!` | ADMIN | 관리자 API 테스트용 계정. 로컬 DB를 리셋(전체 삭제 후 재시딩)해도 회원가입 API로 다시 만들 수 있음(아래 참고) |

**주의**: `dummy_data.sql`로 시딩되는 회원(예: id 1~3번 등)은 더미 데이터 생성기가 만든 임의의 비밀번호 해시라 **실제 로그인 가능한 평문 비밀번호가 없습니다.** ADMIN 권한 테스트가 필요하면 아래처럼 새 계정을 만들어서 쓰세요.

```bash
# 1) 회원가입
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"내이메일@example.com","password":"원하는비번","nickname":"닉네임","birthDate":"1990-01-01"}'

# 2) DB에서 방금 만든 계정을 ADMIN으로 승격
mysql -u root -p sangsangseoga -e "UPDATE member SET role='ADMIN' WHERE email='내이메일@example.com';"
```

## 트러블슈팅

- **서버 재실행 시 컬럼이 중복돼서 생기거나 타입 에러가 남**: `ddl-auto: update`는 없는 테이블/컬럼을 추가만 할 뿐, 기존 컬럼의 이름 변경이나 삭제는 해주지 않습니다. 엔티티 필드명을 바꿨다면 문제되는 테이블만 `DROP TABLE {테이블명}` 하거나, 아래 "전체 테이블 초기화" 방법으로 스키마를 통째로 리셋한 뒤 서버를 재기동하세요.
- **더미 데이터 삽입 시 `Unknown column` / `Table doesn't exist` 에러**: 테이블이 최신 엔티티 기준으로 생성되지 않은 상태입니다. 서버를 한 번 더 실행해 테이블을 갱신하거나, 아래 "전체 테이블 초기화" 방법으로 스키마를 리셋한 뒤 서버를 다시 실행해 테이블을 새로 만드세요.
- **전체 테이블 초기화가 필요할 때(스키마가 꼬여서 더 이상 손대기 어려운 경우)**: `src/main/resources/docs/sql/delete_all_tables.sql`을 실행하면 FK 체크를 잠시 끈 뒤 이 프로젝트의 모든 테이블을 `DROP`합니다. 실행 후 순서는 다음과 같습니다.
  ```bash
  mysql -u root -p sangsangseoga < src/main/resources/docs/sql/delete_all_tables.sql
  ```
  1. 위 스크립트로 테이블을 모두 삭제합니다.
  2. 서버를 한 번 실행해(`bootRun`) 엔티티 기준으로 테이블을 새로 생성합니다.
  3. `dummy_data.sql`을 다시 실행해 더미 데이터를 채웁니다(위 "7. 더미 데이터 삽입" 참고).

  ⚠️ 이 스크립트는 데이터를 포함해 테이블 자체를 삭제하므로, 운영 DB나 남이 쓰고 있는 공용 DB에는 실행하지 마세요. 로컬 개발용 스키마에서만 사용합니다. 또한 `delete_all_tables.sql`의 `DROP TABLE` 목록은 스크립트 작성 시점의 테이블 기준이라, 이후 새로 추가된 도메인/테이블이 있다면 목록에 없어 삭제되지 않을 수 있습니다(그 경우 해당 테이블만 별도로 `DROP TABLE`).
- **한글이 깨져 보임**: MariaDB 스키마 생성 시 `CHARACTER SET utf8mb4`를 빼먹지 않았는지 확인하세요.
- **Redis 관련 연결 에러 / 포트 충돌**: 로컬에 Redis가 이미 떠 있는 상태에서 `docker compose up -d`를 실행하면 6379 포트가 겹쳐 컨테이너가 뜨지 않거나, 서버가 도커 컨테이너가 아닌 로컬 Redis에 연결될 수 있습니다. 로컬 Redis를 먼저 종료(`taskkill`/`net stop Redis`/`systemctl stop redis` 등)한 뒤 컨테이너를 재시작하세요.
- **`docker compose up -d` 실행 시 명령을 찾을 수 없다는 에러**: Docker Desktop이 설치되어 있지 않거나 실행 중이 아닌 경우입니다. Docker Desktop을 켜고 트레이 아이콘이 정상 상태(초록불)인지 확인한 뒤 다시 시도하세요.

## 브랜치 전략

3개 레벨의 브랜치를 사용합니다.

| 브랜치 | 용도 | 비고 |
| --- | --- | --- |
| `main` | 배포(운영) 브랜치 | `develop`에서 검증된 내용만 병합. 직접 커밋 금지 |
| `develop` | 개발 통합 브랜치 | 각 기능 브랜치가 PR로 병합되는 기준 브랜치. 평소 작업은 여기서 분기 |
| `feature/{작업명}` | 기능 개발 브랜치 | `develop`에서 분기, 작업 완료 후 `develop`으로 PR 병합 |

**작업 흐름**

1. `develop`을 최신 상태로 받아온 뒤 브랜치를 분기합니다.
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/{작업명}
   ```
   예: `feature/my-library`, `feature/friend-library`
2. 기능 단위로 작업하고, 하나의 논리적 변경마다 커밋합니다.
3. 작업이 끝나면 원격에 푸시하고 `develop`을 대상으로 PR을 생성합니다.
   ```bash
   git push origin feature/{작업명}
   ```
4. 리뷰(코드리뷰) 후 `develop`에 병합하고, 원격 기능 브랜치는 삭제합니다.
5. `develop`이 배포 가능한 상태로 안정화되면 `develop → main`으로 별도 PR을 통해 병합합니다. `main`에는 배포 시점에만 병합합니다.

## 커밋 컨벤션

`{type}: {작업 내용} (#이슈번호)` 형식을 기본으로 합니다. 이슈 번호는 관련 PR/이슈가 있을 때 붙입니다.

```
feat: 회원가입(signup) API 구현 (#18)
fix: ERD 반영 보완 및 Swagger API 문서 추가 (#21)
```

여러 도메인/세부 작업을 한 커밋에 담을 때는 `type: 도메인 - 작업 내용` 형식도 사용합니다.

```
feat: subscription/ai 도메인 - ERD 반영 및 컨트롤러 분리 (#17)
```

**type 종류**

| type | 의미 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 구조 개선 |
| `chore` | 빌드/설정/의존성 등 코드 외적인 변경 (예: CI, PR 템플릿) |
| `docs` | 문서(README, 주석 등) 수정 |
| `test` | 테스트 코드 추가/수정 |
| `style` | 포맷팅, 세미콜론 등 동작에 영향 없는 코드 스타일 변경 |

**작성 규칙**

- 제목은 한글로 간결하게, 무엇을 했는지 알 수 있도록 작성합니다. (예: "수정", "작업" 같은 모호한 표현 지양)
- 하나의 커밋은 하나의 논리적 변경 단위로 구성합니다. (기능 추가와 무관한 리팩토링은 별도 커밋으로 분리)
- PR 제목도 커밋 컨벤션과 동일한 형식(`type: 작업 내용 (#이슈번호)`)을 따릅니다.
- 이슈 번호가 없는 초기 설정성 커밋(`chore` 등)은 이슈 번호를 생략할 수 있습니다.
