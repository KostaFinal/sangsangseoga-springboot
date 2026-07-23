# 배포 설정 정리 — 로컬 vs 클라우드, 환경변수, GitHub Secrets

"뭘 어디에 설정해야 하는지"만 모아놓은 문서. 왜 그렇게 나뉘는지는 [07-cloud-deployment.md](./07-cloud-deployment.md)에 아키텍처/개념 설명이 있으니 같이 참고.

## 1. 로컬 vs 클라우드 실행 방식

코드는 완전히 동일하고, `spring.profiles.active` 하나로 나머지가 전부 갈린다.

| | 로컬 개발 (`dev`) | 클라우드 운영 (`prod`, EC2) |
| --- | --- | --- |
| 실행 방식 | IDE에서 `SangsangseogaApplication` 직접 실행 / `./gradlew bootRun` | `docker compose -f docker-compose.prod.yml up -d --build` (backend/python-llm/redis 3개 컨테이너) |
| DB | 로컬 설치 MariaDB, `localhost:3306`. `ddl-auto: update`(엔티티 바뀌면 테이블도 자동 반영) | RDS. `ddl-auto: validate`(스키마 미리 반영 필수, 안 맞으면 기동 자체가 실패) |
| Redis | `docker-compose.yml`로 로컬 컨테이너 1개(`localhost:6379`) | `docker-compose.prod.yml`의 `redis` 서비스. AOF+볼륨으로 영속화, 서비스명(`redis`)으로 접근 |
| 파일 저장 | `STORAGE_TYPE=local`, 디스크(`./uploads`)에 저장 | `STORAGE_TYPE=s3`, EC2에 붙은 **IAM Role**로 자동 인증(액세스 키 없음) |
| 파이썬 AI 서버 | 개발자가 직접 `uvicorn app.main:app --reload`, `FASTAPI_BASE_URL=http://localhost:8000` | `python-llm` 컨테이너. 같은 도커 네트워크 안에서 서비스명(`http://python-llm:8000`)으로 접근 — `localhost`로는 컨테이너끼리 서로 못 찾음 |
| HTTPS | 없음 (`http://localhost:8080`) | EC2는 8080 평문. 앞단 **ALB**가 ACM 인증서로 TLS 종료 후 8080으로 포워딩 |
| Swagger | 켜짐 | **꺼짐** (API 구조 노출 방지를 위해 `prod`에서 의도적으로 비활성화) |
| 비밀값 | `application.yml` 기본값으로 대충 굴러감(`root`/`7564` 등) | 전부 필수, 기본값 없음 — `.env.prod`/`.env.python-llm`에 실값 없으면 기동 자체가 실패 |

## 2. 클라우드에서 채워야 하는 환경변수

**전부 GitHub Secrets가 아니라 EC2 위의 `.env.prod` / `.env.python-llm` 파일에 들어간다** (어떻게 넣는지는 3번, GitHub Secrets가 왜 필요 없는지는 4번 참고).

### 2-1. `backend` — `.env.prod` (`.env.prod.example` 템플릿 기준)

| 변수 | 필수 | 어디서 발급/확인 | 설명 |
| --- | --- | --- | --- |
| `PROD_DB_HOST` | ✅ | RDS 콘솔 | RDS 엔드포인트 |
| `PROD_DB_USERNAME` / `PROD_DB_PASSWORD` | ✅ | RDS 생성 시 직접 지정 | DB 계정 |
| `JWT_SECRET_KEY` | ✅ | 직접 생성(32자 이상 랜덤 문자열) | 로컬 기본값(`defaultSecretKey...`)을 운영에 그대로 쓰면 안 됨 — 토큰 위조 위험 |
| `FRONTEND_URL` | ✅ | 프론트 배포 후 확정된 도메인 | CORS 허용 오리진, 메일 링크(`/reset-password?token=`) 등에 사용 |
| `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` | ✅ | SMTP 제공자(Gmail/SES 등) | 비밀번호 재설정·보호자 동의 메일 발송용 |
| `GEMINI_API_KEY` | ✅ | Google AI Studio | 실제 텍스트 생성 흐름(`AiService`)은 파이썬 서버를 호출해서 이 값을 안 쓰지만, `GeminiServiceImpl`이 `@Service` + `@Value("${gemini.api.key}")`라 스프링 컨텍스트 기동 시 무조건 빈이 생성됨 — 값이 없으면 플레이스홀더 해석 실패로 **애플리케이션이 아예 안 뜬다** |
| `STORAGE_TYPE` | ✅ | 고정값 `s3` | 로컬(`local`)과 달리 운영은 S3 사용 |
| `S3_BUCKET` / `S3_REGION` / `S3_PUBLIC_BASE_URL` | ✅ | S3/CloudFront 콘솔 | 업로드 파일 저장 위치. 액세스 키는 안 넣음(IAM Role이 대신함) |
| `KAKAO_CLIENT_ID` / `KAKAO_CLIENT_SECRET` | 선택 | 카카오 디벨로퍼스 → 내 애플리케이션 → 앱 키(REST API 키) / 카카오 로그인 → 보안 | 값이 없으면 서버는 정상 기동하지만(`oauth.kakao.client-id` 기본값이 빈 문자열) 카카오 로그인 시도 시 `OAUTH_NOT_CONFIGURED`. Client Secret은 콘솔에서 활성화한 경우에만 필요 |
| `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` | 선택 | 네이버 개발자센터 → 내 애플리케이션 | 위와 동일, 값 없으면 네이버 로그인만 `OAUTH_NOT_CONFIGURED` |

### 2-2. `python-llm` — `.env.python-llm` (`.env.python-llm.example` 템플릿 기준)

| 변수 | 필수 | 설명 |
| --- | --- | --- |
| `GEMINI_API_KEY` | ✅ | 텍스트/이미지 생성 호출에 실제로 쓰이는 키(백엔드와 별개로 이 컨테이너가 직접 Gemini를 호출) |
| `GEMINI_MODEL` | ✅ | 기본값 `gemini-2.5-flash` |
| `CORS_ALLOWED_ORIGINS` | ✅ | 운영 프론트 도메인(콤마로 여러 개 가능). 기본값은 로컬 개발용(`localhost:5173`)이라 운영에선 반드시 덮어써야 함 |

`FASTAPI_BASE_URL`은 `docker-compose.prod.yml`이 `http://python-llm:8000`으로 자동 주입하므로 `.env.prod`에 직접 안 넣어도 된다.

## 3. EC2에 값을 채워넣는 방법

`.env.prod`/`.env.python-llm`은 git에 올라가지 않는 파일(`.gitignore`에 `.env*` 처리됨)이라, **EC2 로컬 디스크에 직접 만들어야 한다.** GitHub에 등록하는 값이 아니다.

### 방법 1 — SSH 접속 후 직접 작성 (초기엔 이걸로 충분, 권장)

```bash
ssh -i my-key.pem ec2-user@<EC2 IP>
cd ~/be   # docker-compose.prod.yml이 있는 위치
nano .env.prod          # .env.prod.example 참고해서 실제 값 입력 후 저장
nano .env.python-llm    # .env.python-llm.example 참고해서 작성
```

`docker compose -f docker-compose.prod.yml up -d`가 이 파일을 그대로 읽는다.

### 방법 2 — 로컬에서 작성 후 `scp`로 업로드

```bash
scp -i my-key.pem .env.prod ec2-user@<EC2 IP>:~/be/.env.prod
scp -i my-key.pem .env.python-llm ec2-user@<EC2 IP>:~/be/.env.python-llm
```

방법 1보다 편하지만, 로컬 PC에도 비밀값이 파일로 남는다는 점은 감안할 것.

### 방법 3 — AWS Secrets Manager / SSM Parameter Store (더 정석적인 방식)

EC2 부팅 스크립트가 Secrets Manager에서 값을 읽어와 그 자리에서 `.env.prod`를 생성. 비밀값이 평문 파일로 오래 남지 않고, IAM 권한으로 접근 통제가 되며, 값이 바뀌어도 EC2를 재빌드하면 자동 반영되는 장점이 있다. 다만 초기 설정(Secrets Manager 등록 + 부팅 스크립트 작성)이 방법 1/2보다 손이 더 감.

**지금 단계(첫 배포)는 방법 1로 충분하고, 인스턴스를 자주 새로 만들거나 팀 규모가 커지면 방법 3으로 넘어가는 걸 고려한다.**

## 4. CD — CI 통과 후에만 자동 배포

`be`/`python` 두 리포 모두 `.github/workflows/cd.yml`이 있다. 트리거는 `push`가 아니라 **같은 커밋에서 `CI` 워크플로우가 성공적으로 끝났을 때**(`workflow_run` 이벤트)다 — `main`에 뭔가 올라가는 즉시 검증 없이 배포되는 걸 막기 위해, "빌드/컴파일 검증 통과"를 배포의 필수 조건으로 걸어뒀다.

- `be`: `ci.yml`이 `./gradlew compileJava compileTestJava`를 돌리고, 통과해야 `cd.yml`이 이어서 실행된다.
- `python`: 원래 CI가 없었어서 이번에 `ci.yml`을 새로 만들었다 — `pip install -r requirements.txt`(의존성 깨짐 검출), `python -m compileall app`(문법 오류 검출), `docker build`(배포 이미지가 실제로 빌드되는지 검증)까지 통과해야 `cd.yml`이 실행된다. `python` 리포는 `develop` 없이 `main`에 직접 push하는 구조라, 이 게이트가 없으면 push=즉시 운영 배포나 마찬가지였다.

배포 job엔 `if: github.event.workflow_run.conclusion == 'success'` 조건이 걸려 있어서, CI가 실패하면 CD는 아예 실행되지 않는다.

어느 리포에서 push하든 **두 리포 모두 최신화한 뒤 전체 스택을 재빌드**한다 — `docker-compose.prod.yml`의 `python-llm` 서비스가 `../sangsangseoga-python`을 빌드 컨텍스트로 쓰기 때문에, `be`만 최신화하고 `sangsangseoga-python`이 오래된 채로 두면 배포판과 실제 코드가 어긋날 수 있어서다.

외부 마켓플레이스 액션(`appleboy/ssh-action` 등) 대신 GitHub Actions 러너에 기본 내장된 `ssh`/`ssh-keyscan`만 사용했다 — 서드파티 액션에 배포 권한(SSH 키)을 넘기지 않기 위함.

### 추가 안전장치를 원한다면 — 수동 승인 게이트

자동 검증(컴파일/빌드 통과)만으론 부족하고 "사람이 한 번 더 확인 후 배포"를 원하면, `deploy` job에 `environment: production`을 추가하고 GitHub 저장소 Settings → Environments에서 `production` 환경에 **Required reviewers**를 지정하면 된다. 그러면 CI가 통과해도 CD가 곧장 실행되지 않고, 지정된 리뷰어가 Actions 탭에서 승인 버튼을 눌러야 배포가 진행된다. 지금은 안 걸어뒀다.

### 필요한 GitHub Secrets (Repository → Settings → Secrets and variables → Actions)

**`be`, `python` 두 리포 모두 동일하게 등록해야 한다.**

| Secret | 값 | 비고 |
| --- | --- | --- |
| `EC2_HOST` | EC2 퍼블릭 IP 또는 도메인 | |
| `EC2_USER` | EC2 접속 계정 (예: `ec2-user`, `ubuntu`) | AMI 종류에 따라 다름 |
| `EC2_SSH_PRIVATE_KEY` | 배포 전용 SSH **개인키**를 base64로 인코딩한 값 | 아래 "최초 1회 설정" 참고. 로그인용 `.pem` 키를 그대로 쓰지 말고 배포 전용 키를 새로 만들 것. 원본 PEM(줄바꿈 포함)을 그대로 Secret에 붙여넣으면 복사 과정에서 개행이 깨져 `cd.yml`이 SSH 접속 시 "error in libcrypto"로 실패하므로, 반드시 base64 한 줄로 인코딩해서 등록한다(`cd.yml`이 `base64 -d`로 복원) |

`.env.prod`/`.env.python-llm` 안의 값(DB 비밀번호, JWT 시크릿 등)은 **GitHub Secrets로 옮기지 않는다** — 계속 EC2 서버에만 있고, GitHub Actions는 "배포를 실행시키는 권한"(SSH 접속)만 가지면 된다.

### 최초 1회 설정 (EC2 콘솔/SSH에서)

1. 배포 전용 키 쌍 생성 (로컬 또는 EC2에서):
   ```bash
   ssh-keygen -t ed25519 -f deploy_key -N ""
   ```
2. **공개키**(`deploy_key.pub`)를 EC2의 `~/.ssh/authorized_keys`에 추가
3. **개인키**(`deploy_key`)를 base64로 인코딩해서 `EC2_SSH_PRIVATE_KEY` GitHub Secret에 등록 (`be`, `python` 두 리포 다):
   ```bash
   base64 -w0 deploy_key   # 출력된 한 줄짜리 값을 그대로 Secret에 붙여넣는다
   ```
4. EC2에 두 리포를 각각 `~/be`, `~/sangsangseoga-python`로 clone, `.env.prod`/`.env.python-llm` 작성(2, 3번 항목 참고), Docker/Docker Compose 설치
5. 최초 1회는 수동으로 `docker compose -f docker-compose.prod.yml up -d --build`를 돌려서 정상 기동을 확인 — 그 이후부터 `main` push마다 CD가 같은 명령을 대신 실행한다
