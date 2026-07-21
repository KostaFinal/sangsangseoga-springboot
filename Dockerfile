# ---------------------------------------------------------------------------
# 1단계: 빌드
# 로컬/CI에서 만든 산출물이 아니라 컨테이너 안에서 직접 gradle build를 돌려
# "내 로컬에서는 됐는데" 문제를 없앤다.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:11-jdk AS build
WORKDIR /workspace

# 의존성 레이어를 소스코드 레이어보다 먼저 캐싱해서, 소스만 바뀐 재빌드 시
# gradle dependency 다운로드를 반복하지 않게 한다.
COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

# ---------------------------------------------------------------------------
# 2단계: 실행
# JDK가 아니라 JRE만 있으면 되고, war/jar 하나만 필요하므로 이미지를 최소화한다.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:11-jre
WORKDIR /app

# war 플러그인이 붙어 있지만 embedded Tomcat을 포함하므로 java -jar로 그대로 실행 가능하다.
COPY --from=build /workspace/build/libs/*.war app.war

# 로컬 디스크 업로드 경로(STORAGE_TYPE=local일 때만 실사용, prod는 S3라 볼륨 없어도 됨).
RUN mkdir -p /app/uploads

EXPOSE 8080

# 배포 환경변수(PROD_DB_HOST, JWT_SECRET_KEY, SPRING_PROFILES_ACTIVE 등)는 이미지에 굽지 않고
# docker-compose/EC2 환경변수 또는 SSM으로 주입한다.
# SPRING_PROFILES_ACTIVE는 Spring Boot가 별도 인자 없이도 환경변수에서 그대로 읽는다.
ENTRYPOINT ["java", "-jar", "/app/app.war"]
