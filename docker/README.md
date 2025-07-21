# Docker 환경 구성 가이드

이 문서는 MSA E-Commerce Customer 서비스의 Docker 환경 구성 방법을 설명합니다.

## 구성 요소

- **MariaDB 11.2**: 고객 데이터 저장
- **Redis 7.2**: 캐싱 및 세션 관리
- **Apache Kafka 7.5.3**: 이벤트 기반 메시징
- **Zookeeper**: Kafka 클러스터 관리
- **Kafka UI**: Kafka 모니터링 (선택사항)
- **Customer API**: Spring Boot 애플리케이션

## 빠른 시작

### 1. 환경 변수 설정

```bash
# .env 파일 생성
cp .env.example .env

# .env 파일을 열어 필요한 값 수정
# 특히 JWT_SECRET은 프로덕션에서 반드시 변경해야 합니다.
```

### 2. Docker Compose 실행

```bash
# 모든 서비스 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 특정 서비스 로그 확인
docker-compose logs -f customer-api
```

### 3. 서비스 상태 확인

```bash
# 모든 컨테이너 상태 확인
docker-compose ps

# 헬스체크 상태 확인
docker ps --format "table {{.Names}}\t{{.Status}}"
```

## 서비스 접속 정보

| 서비스 | 포트 | 접속 URL/정보 |
|--------|------|---------------|
| Customer API | 8080 | http://localhost:8080 |
| MariaDB | 3306 | `mysql -h localhost -P 3306 -u customer_user -p` |
| Redis | 6379 | `redis-cli -h localhost -p 6379 -a redis_password` |
| Kafka | 9092 | localhost:9092 |
| Kafka UI | 8090 | http://localhost:8090 |

## 개발 팁

### 애플리케이션만 재시작

```bash
# 애플리케이션 재빌드 및 재시작
docker-compose up -d --build customer-api
```

### 데이터베이스 초기화

```bash
# 볼륨 삭제 (주의: 모든 데이터가 삭제됩니다)
docker-compose down -v

# 다시 시작
docker-compose up -d
```

### 로컬 개발 모드

로컬에서 개발 시 인프라만 Docker로 실행하고 애플리케이션은 IDE에서 실행할 수 있습니다:

```bash
# 인프라 서비스만 실행
docker-compose up -d mariadb redis kafka zookeeper

# application-dev.yml에서 다음과 같이 설정
# spring.datasource.url: jdbc:mariadb://localhost:3306/customer_db
# spring.data.redis.host: localhost
# spring.kafka.bootstrap-servers: localhost:9092
```

### Kafka 토픽 관리

```bash
# Kafka 컨테이너 접속
docker exec -it customer-kafka bash

# 토픽 목록 확인
kafka-topics --list --bootstrap-server localhost:9092

# 토픽 생성
kafka-topics --create --topic customer-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1

# 메시지 확인
kafka-console-consumer --topic customer-events --from-beginning --bootstrap-server localhost:9092
```

## 문제 해결

### 서비스가 시작되지 않는 경우

1. 포트 충돌 확인
```bash
# 사용 중인 포트 확인
lsof -i :8080
lsof -i :3306
lsof -i :6379
lsof -i :9092
```

2. Docker 리소스 확인
```bash
# Docker 상태 확인
docker system df

# 불필요한 리소스 정리
docker system prune -a
```

### 메모리 부족 문제

Docker Desktop의 메모리 할당을 최소 4GB 이상으로 설정하세요.

### 로그 확인

```bash
# 전체 로그
docker-compose logs

# 실시간 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs customer-api
```

## 프로덕션 고려사항

1. **보안**
   - 모든 기본 비밀번호 변경
   - JWT_SECRET 강력한 값으로 변경
   - 네트워크 격리 구성

2. **성능**
   - JVM 힙 메모리 조정
   - 데이터베이스 커넥션 풀 설정
   - Redis 메모리 제한 설정

3. **모니터링**
   - Prometheus + Grafana 추가
   - ELK 스택 통합
   - 분산 추적 (Zipkin/Jaeger)

## 유용한 명령어

```bash
# 전체 스택 중지
docker-compose down

# 전체 스택 중지 및 볼륨 삭제
docker-compose down -v

# 특정 서비스만 재시작
docker-compose restart customer-api

# 스케일 아웃 (Kafka UI 제외)
docker-compose up -d --scale customer-api=3

# 컨테이너 내부 접속
docker exec -it customer-api sh
```