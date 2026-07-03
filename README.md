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
- Redis (기본 포트 6379)
- Git

### 2. 저장소 클론

```bash
git clone <저장소 주소>
cd sangsangseoga-springboot
```

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
| `JWT_SECRET_KEY` | 기본 문자열 제공 | JWT 서명 키 |
| `GEMINI_API_KEY` | 없음 (**필수**) | Gemini API 키. 없으면 AI 생성 기능만 실패하고 나머지 서버 구동에는 문제 없음 |

환경 변수는 아래 중 편한 방법으로 설정하면 됩니다.

**IntelliJ 사용 시**: Run/Debug Configurations → Environment variables 항목에 `GEMINI_API_KEY=발급받은키` 형식으로 입력

**터미널(Mac/Linux) 사용 시**
```bash
export GEMINI_API_KEY=발급받은키
```

**터미널(Windows PowerShell) 사용 시**
```powershell
$env:GEMINI_API_KEY="발급받은키"
```

운영(`prod`) 프로필은 `PROD_DB_HOST`, `PROD_DB_USERNAME`, `PROD_DB_PASSWORD`, `PROD_REDIS_HOST`, `JWT_SECRET_KEY`를 반드시 환경변수로 주입해야 서버가 뜹니다(기본값 없음).

### 5. 서버 최초 실행 (테이블 자동 생성)

`spring.jpa.hibernate.ddl-auto=update` 설정 덕분에, 서버를 한 번 기동하면 엔티티 기준으로 테이블이 자동 생성됩니다. 별도의 DDL 스크립트를 실행할 필요가 없습니다.

```bash
# Mac/Linux
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

콘솔에 `Started SangsangseogaApplication` 같은 로그가 뜨고 에러 없이 실행되면, MariaDB에 테이블이 전부 생성된 것입니다. 확인 후 `Ctrl+C`로 서버를 종료합니다.

### 6. 더미 데이터 삽입

테이블 생성이 끝난 뒤 `src/main/resources/docs/sql/dummy_data.sql`을 실행합니다. 테이블 간 참조(FK) 순서에 맞춰 작성되어 있으므로 **전체를 한 번에 실행**해야 합니다.

```bash
mysql -u root -p sangsangseoga < src/main/resources/docs/sql/dummy_data.sql
```

(GUI 툴을 쓴다면 DBeaver, HeidiSQL 등에서 해당 파일을 열어 전체 실행해도 됩니다.)

### 7. 서버 재실행

```bash
./gradlew bootRun        # Mac/Linux
gradlew.bat bootRun       # Windows
```

## API 문서 (Swagger)

서버 실행 후 아래 주소에서 전체 API를 확인/테스트할 수 있습니다.

```
http://localhost:8080/swagger-ui/index.html
```

로그인(`/api/auth/login`)으로 발급받은 Access Token을 우측 상단 `Authorize` 버튼에 입력하면, 이후 모든 요청에 `Authorization: Bearer <token>` 헤더가 자동으로 포함됩니다.

## 트러블슈팅

- **서버 재실행 시 컬럼이 중복돼서 생기거나 타입 에러가 남**: `ddl-auto: update`는 없는 테이블/컬럼을 추가만 할 뿐, 기존 컬럼의 이름 변경이나 삭제는 해주지 않습니다. 엔티티 필드명을 바꿨다면 해당 테이블을 직접 `DROP TABLE {테이블명}` 후 서버를 재기동하세요.
- **더미 데이터 삽입 시 `Unknown column` 에러**: 테이블이 최신 엔티티 기준으로 생성되지 않은 상태입니다. 서버를 한 번 더 실행해 테이블을 갱신하거나, 문제되는 테이블을 지우고 다시 생성하세요.
- **한글이 깨져 보임**: MariaDB 스키마 생성 시 `CHARACTER SET utf8mb4`를 빼먹지 않았는지 확인하세요.

## 브랜치 / 커밋 컨벤션

- 브랜치: `feature/{작업명}` (예: `feature/my-library`)
- 커밋/PR 제목: `feat: {작업 내용} (#이슈번호)` 형식
