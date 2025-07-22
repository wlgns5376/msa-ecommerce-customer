# 커머스 마이크로서비스 - 고객/인증 서비스

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=spring)
![Test Coverage](https://img.shields.io/badge/Test%20Coverage-72%25-brightgreen?style=flat-square&logo=codecov)
![Build](https://img.shields.io/badge/Build-Gradle%208.5-blue?style=flat-square&logo=gradle)
![Architecture](https://img.shields.io/badge/Architecture-Hexagonal%20DDD-purple?style=flat-square)

## 📋 프로젝트 개요

이 프로젝트는 이커머스 플랫폼을 위한 고객 도메인 마이크로서비스입니다. 헥사고날 아키텍처와 DDD(Domain-Driven Design)를 기반으로 하는 4계층 아키텍처로 설계되었습니다.

### 🛠 기술 스택

- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **빌드 도구**: Gradle 8.5
- **테스트**: JUnit 5, Mockito, AssertJ
- **보안**: Spring Security, JWT (jsonwebtoken)
- **데이터베이스**: H2 (개발), PostgreSQL (운영)
- **캐싱**: Redis
- **메시징**: Kafka

## 🏗 아키텍처

4계층 아키텍처와 헥사고날 아키텍처를 기반으로 설계되었습니다.

```
├── bootstrap/          # 애플리케이션 진입점 (API 서버)
│   └── customer-api/   # 고객 서비스 REST API
├── core/              # 도메인 핵심 로직
│   └── customer-core/ # 고객 도메인 모델 & 비즈니스 로직
├── infrastructure/    # 외부 의존성 구현
│   └── persistence/   # 데이터 영속성 계층
└── common/           # 공통 모듈
```

> 📖 **상세 아키텍처 설계**: [architecture-overview.md](docs/architecture-overview.md)를 참조하세요.

## 🎯 주요 기능

### 현재 구현된 기능

#### 🌐 REST API 서비스 (customer-api)
- **계정 관리 API**: 회원가입, 로그인, 로그아웃, 토큰 갱신
- **프로필 관리 API**: 고객 프로필 CRUD, 주소 관리
- **API 문서화**: Swagger/OpenAPI 3.0 기반 API 문서
- **예외 처리**: 글로벌 예외 핸들러와 표준화된 에러 응답

#### 🏛️ 고객 도메인 (customer-core)
- **계정 관리**: 고객 계정 생성, 활성화, 상태 관리
- **인증/인가**: JWT 기반 토큰 인증 시스템
- **프로필 관리**: 개인정보, 주소, 연락처, 선호도 관리
- **애플리케이션 서비스**: 비즈니스 로직 조합 및 트랜잭션 관리
- **도메인 이벤트**: 계정 생성, 활성화, 로그인 성공 등

#### 🔌 API 엔드포인트

**계정 관리 API (`/api/v1/accounts`)**
- `POST /` - 계정 생성
- `POST /login` - 로그인
- `POST /logout` - 로그아웃
- `POST /refresh` - 토큰 갱신
- `GET /me` - 계정 정보 조회

**프로필 관리 API (`/api/v1/profiles`)**
- `POST /` - 프로필 생성
- `GET /me` - 현재 사용자 프로필 조회
- `GET /{profileId}` - 특정 프로필 조회
- `POST /{profileId}/addresses` - 주소 추가
- `PUT /{profileId}/addresses/{addressId}` - 주소 수정

> 📚 **상세 도메인 구조**: [architecture-overview.md](docs/architecture-overview.md)를 참조하세요.

### 향후 계획

1. ✅ **고객 프로필 관리**: 개인정보, 주소, 연락처 관리 (완료)
2. ✅ **Customer Service API**: REST API 엔드포인트 구현 (완료)
3. **고객 활동 추적**: 로그인 기록, 페이지 뷰, 구매 패턴
4. **고객 세분화**: RFM 분석, 행동 기반 세분화
5. **고객 라이프사이클 관리**: 온보딩, 유지, 이탈 관리
6. **고객 소통 관리**: 알림, 메시지, 피드백 시스템

## 🚀 시작하기

### 전제 조건

- Java 17 이상
- Gradle 8.5 이상

### 빌드 및 실행

```bash
# 전체 빌드
./gradlew build

# 특정 모듈 빌드
./gradlew :customer-core:build
./gradlew :customer-api:build

# API 서버 실행
./gradlew :customer-api:bootRun

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

### API 서버 접속

```bash
# 개발 환경
http://localhost:8080

# Swagger UI
http://localhost:8080/swagger-ui.html

# API 문서
http://localhost:8080/api-docs
```

### 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :customer-core:test

# 테스트 커버리지 확인
./gradlew jacocoTestReport

# 특정 테스트 클래스만 실행
./gradlew test --tests "*.AccountApplicationServiceTest"

# 테스트 결과 리포트 보기 (브라우저에서 열기)
open core/customer-core/build/reports/tests/test/index.html

# 테스트 커버리지 리포트 보기 (브라우저에서 열기)
open core/customer-core/build/reports/jacoco/test/html/index.html
```

## 📝 개발 가이드

### 코딩 규칙

1. **Lombok 사용**: 모든 엔티티/값객체에 `@Getter` 적용
2. **불변 객체**: 값 객체는 불변으로 설계
3. **유효성 검증**: 생성자에서 비즈니스 규칙 검증
4. **도메인 이벤트**: 중요한 상태 변경 시 이벤트 발행
5. **패키지 구조**: domain, application, usecase로 명확히 분리

### 테스트 코드 규칙

- Given-When-Then 패턴 적용
- 예외 상황 테스트 포함
- `@DisplayName` 사용하여 테스트 가독성 향상
- Parameterized Tests 적절히 활용

### 커밋 규칙

- push 하기 전에 수정된 모듈의 테스트 코드가 모두 통과해야 함
- 테스트 커버리지 70% 이상 유지 (현재: 72%)

### 테스트 현황

- **전체 테스트 수**: 200+ 테스트
- **테스트 커버리지**: 72%
- **프로필 도메인 커버리지**: 83%

## 🔧 설정

### 환경별 설정 파일

- `application.yml`: 기본 설정
- `application-dev.yml`: 개발 환경
- `application-test.yml`: 테스트 환경  
- `application-prod.yml`: 운영 환경

### JaCoCo 테스트 커버리지

- 최소 커버리지: 80%
- 리포트 형식: XML, HTML

## 📋 향후 계획

### 예정된 인프라스트럭처

1. **messaging**: Kafka 메시징
2. **external**: 외부 API 연동
3. **cache**: Redis 캐싱

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 🤝 기여하기

1. 이슈를 먼저 등록해 주세요
2. 브랜치를 생성하여 작업해 주세요
3. 테스트 코드를 작성해 주세요
4. Pull Request를 생성해 주세요

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해 주세요.