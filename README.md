# 커머스 마이크로서비스 - 고객/인증 서비스

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

### 4계층 아키텍처

```
├── bootstrap/          # 애플리케이션 진입점 (API 서버)
├── core/              # 도메인 핵심 로직
├── infrastructure/    # 외부 의존성 구현
└── common/           # 공통 모듈
```

### 의존성 규칙

- ✅ Bootstrap → Core, Infrastructure, Common
- ✅ Infrastructure → Core, Common  
- ✅ Core → Common
- ❌ Core 모듈 간 직접 의존 금지
- ❌ Common → 다른 모듈 의존 금지

## 📂 프로젝트 구조

```
msa_ecommerce_customer/
├── common/                     # 공통 모듈
│   ├── src/main/java/
│   └── build.gradle
├── core/                       # 핵심 비즈니스 로직
│   └── customer-core/          # 고객 도메인 
│       ├── src/main/java/com/commerce/customer/core/
│       │   ├── application/    # 애플리케이션 서비스
│       │   ├── domain/         # 도메인 모델
│       │   │   ├── event/      # 도메인 이벤트
│       │   │   ├── exception/  # 도메인 예외
│       │   │   ├── model/      # 도메인 엔티티/값객체
│       │   │   ├── repository/ # 리포지토리 인터페이스
│       │   │   └── service/    # 도메인 서비스
│       │   └── usecase/        # 유스케이스
│       └── build.gradle
├── infrastructure/             # 인프라스트럭처 계층
│   └── persistence/            # 데이터 영속성
│       ├── src/main/java/
│       │   └── com/commerce/infrastructure/persistence/
│       │       ├── common/     # 공통 엔티티
│       │       └── config/     # JPA 설정
│       └── build.gradle
├── docs/                       # 문서
│   └── prd/                    # 제품 요구사항 문서
├── build.gradle               # 루트 빌드 스크립트
├── settings.gradle            # 프로젝트 설정
└── CLAUDE.md                  # 프로젝트 메모리
```

## 🎯 주요 기능

### 현재 구현된 기능

#### 고객 도메인 (customer-core)
- **계정 관리**: 고객 계정 생성, 활성화, 상태 관리
- **인증/인가**: JWT 기반 토큰 인증 시스템
- **보안**: 비밀번호 암호화, 토큰 만료 관리
- **도메인 이벤트**: 계정 생성, 활성화, 로그인 성공 등

#### 주요 도메인 객체
- `Account`: 고객 계정 엔티티
- `Email`, `Password`: 값 객체
- `JwtToken`, `TokenPair`: JWT 토큰 관리
- `AccountStatus`: 계정 상태 열거형

#### 도메인 이벤트
- `AccountCreatedEvent`: 계정 생성 이벤트
- `AccountActivatedEvent`: 계정 활성화 이벤트
- `LoginSuccessfulEvent`: 로그인 성공 이벤트
- `TokenGeneratedEvent`: 토큰 생성 이벤트

### 계획된 기능 (PRD 기반)

1. **고객 프로필 관리**: 개인정보, 주소, 연락처 관리
2. **고객 활동 추적**: 로그인 기록, 페이지 뷰, 구매 패턴
3. **고객 세분화**: RFM 분석, 행동 기반 세분화
4. **고객 라이프사이클 관리**: 온보딩, 유지, 이탈 관리
5. **고객 소통 관리**: 알림, 메시지, 피드백 시스템

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

# 테스트 실행
./gradlew test

# 클린 빌드
./gradlew clean build
```

### 테스트

```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :customer-core:test

# 테스트 커버리지 확인
./gradlew jacocoTestReport
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
- 테스트 커버리지 80% 이상 유지

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

### 예정된 모듈

1. **product-core**: 상품 도메인
2. **order-core**: 주문 도메인
3. **inventory-core**: 재고 도메인
4. **cart-core**: 장바구니 도메인

### 예정된 인프라스트럭처

1. **messaging**: Kafka 메시징
2. **external**: 외부 API 연동
3. **cache**: Redis 캐싱

### 예정된 Bootstrap 모듈

1. **customer-api**: 고객 서비스 API
2. **product-api**: 상품 서비스 API
3. **order-api**: 주문 서비스 API
4. **gateway**: API 게이트웨이

## 📄 라이선스

이 프로젝트는 MIT 라이선스를 따릅니다.

## 🤝 기여하기

1. 이슈를 먼저 등록해 주세요
2. 브랜치를 생성하여 작업해 주세요
3. 테스트 코드를 작성해 주세요
4. Pull Request를 생성해 주세요

## 📞 문의

프로젝트 관련 문의사항이 있으시면 이슈를 등록해 주세요.