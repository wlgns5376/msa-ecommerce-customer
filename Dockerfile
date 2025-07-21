# Multi-stage build for Spring Boot application
# Stage 1: Build stage
FROM gradle:8.5-jdk17-alpine AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper 및 빌드 설정 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# 모듈별 빌드 파일 복사
COPY common/build.gradle common/
COPY core/customer-core/build.gradle core/customer-core/
COPY infrastructure/kafka/build.gradle infrastructure/kafka/
COPY infrastructure/persistence/build.gradle infrastructure/persistence/
COPY bootstrap/customer-api/build.gradle bootstrap/customer-api/

# 의존성 다운로드 (캐싱 활용)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY common/src common/src
COPY core/customer-core/src core/customer-core/src
COPY infrastructure/kafka/src infrastructure/kafka/src
COPY infrastructure/persistence/src infrastructure/persistence/src
COPY bootstrap/customer-api/src bootstrap/customer-api/src

# 애플리케이션 빌드
RUN ./gradlew :bootstrap:customer-api:build -x test --no-daemon

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

# 필요한 패키지 설치
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# 한국 시간대 설정
ENV TZ=Asia/Seoul
RUN cp /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 애플리케이션 사용자 생성
RUN addgroup -g 1000 -S spring && \
    adduser -u 1000 -S spring -G spring

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder --chown=spring:spring /app/bootstrap/customer-api/build/libs/*.jar app.jar

# 애플리케이션 사용자로 전환
USER spring:spring

# 컨테이너 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 설정
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# 애플리케이션 포트
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]