# Claude Code Project Memory

## 공통
당신은 Java 백엔드 엔지니어 입니다.
모든 답변은 한국어로 작성한다.

## 프로젝트 개요
- **프로젝트명**: 커머스 마이크로서비스 고객/인증 서비스 4계층 아키텍처
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **빌드 도구**: Gradle 8.5
- **아키텍처**: 4계층 아키텍처 (Bootstrap, Core, Infrastructure, Common)

## 프로젝트 구조
```
msa_ecommerce_customer/
├── common/                     # 공통 모듈
├── core/                       # 핵심 비즈니스 로직
│   └── customer-core/          # 고객 도메인 
│       ├── src/main/java/com/commerce/customer/core/
│       │   ├── domain/         # 도메인 모델
│       │   │   ├── event/      # 도메인 이벤트
│       │   │   ├── exception/  # 도메인 예외
│       │   │   ├── model/      # 도메인 엔티티/값객체
│       │   │   │   ├── profile/ # 프로필 도메인
│       │   │   │   │   ├── Address.java
│       │   │   │   │   ├── CustomerProfile.java
│       │   │   │   │   ├── PersonalInfo.java
│       │   │   │   │   ├── ContactInfo.java
│       │   │   │   │   ├── ProfilePreferences.java
│       │   │   │   │   ├── MarketingConsent.java
│       │   │   │   │   └── NotificationSettings.java
│       │   │   │   └── jwt/     # JWT 도메인
│       │   │   ├── repository/ # 리포지토리 인터페이스
│       │   │   └── service/    # 도메인 서비스
│       │   └── usecase/        # 유스케이스
│       └── src/test/java/      # 테스트 코드 (200+ 테스트)
├── infrastructure/             # 인프라스트럭처 계층
└── docs/                       # 문서
```

## 빌드 명령어
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

## 테스트 명령어
```bash
# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :customer-core:test

# 테스트 커버리지 리포트 생성
./gradlew :customer-core:jacocoTestReport

# 통합 테스트 (아직 없음)
./gradlew integrationTest
```

## 주요 기술 스택
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Lombok**: 코드 간소화 (모든 도메인 객체에 @Getter 적용)
- **JUnit 5**: 테스트 프레임워크
- **H2/MariaDB**: 데이터베이스
- **Redis**: 캐싱 및 세션 관리
- **Kafka**: 메시징 시스템

## 아키텍처 특징
1. **헥사고날 아키텍처**: 포트와 어댑터 패턴
2. **DDD**: 도메인 주도 설계
3. **이벤트 기반**: 도메인 이벤트를 통한 느슨한 결합

## 개발 규칙
1. 설계를 먼저 작성하고, 그에 맞는 테스트 코드를 작성한 후 실제 구현
2. 필요 시 mermaid 다이어그램을 사용하여 명확한 구조 정의
3. Lombok 을 적용하여 코드 간소화
   - @Data 사용은 지양 (불필요한 equals/hashCode 생성 방지)
4. 불변 객체: 값 객체는 불변으로 설계
5. 유효성 검증: 생성자에서 비즈니스 규칙 검증
6. 도메인 이벤트: 중요한 상태 변경 시 이벤트 발행
7. Core 패키지 구조: domain, application, usecase로 명확히 분리
8. 성능 최적화: 쿼리 최적화, 인덱스 설정 등 고려, JPA N+1 고려, 필요 시 QueryDSL 사용
9. 클린 코드 원칙 준수: 가독성, 유지 보수성, 테스트 용이성, 확장 가능성 고려
10. GitHub App 인증 사용: 모든 GitHub 작업은 GitHub App 인증으로 수행

## 테스트 코드 규칙
- Given-When-Then 패턴 적용
- 예외 상황 테스트 포함
- DisplayName 사용하여 테스트 가독성 향상
- Parameterized Tests 를 적절히 활용
- 의존성 추상화: 시간, 외부 시스템 등 테스트 제어성 확보
- 실제 구현 재사용: 테스트용 별도 구현 금지

## 시간 의존적 테스트 방법론
### 핵심 원칙
- **실제 구현 100% 재사용**: 테스트용 별도 구현 생성 금지
- **의존성 추상화**: Clock, Random 등 외부 의존성을 추상화하여 테스트 제어성 확보
- **Clock 패턴**: 시간 관련 테스트는 Clock 의존성 주입으로 해결

### 구현 패턴
```java
// 서비스 클래스 - Clock 의존성 주입
public class SomeService {
    private final Clock clock;
    
    public SomeService(Clock clock) {
        this.clock = clock;
    }
    
    public SomeService() {
        this(Clock.systemDefaultZone()); // 기본 생성자
    }
    
    private SomeResult doSomething() {
        LocalDateTime now = LocalDateTime.now(clock); // Clock 사용
        // 실제 비즈니스 로직
    }
}

// 테스트 코드 - 시간 조작으로 실제 구현 재사용
@Test
void testExpiredScenario() {
    // Given: 과거 시간으로 설정된 Clock 주입
    Clock pastClock = Clock.fixed(Instant.now().minus(8, ChronoUnit.DAYS), ZoneId.systemDefault());
    SomeService pastService = new SomeService(pastClock);
    
    // 실제 구현 메서드 사용하여 만료된 데이터 생성
    SomeResult expiredResult = pastService.doSomething();
    
    // When & Then: 현재 시간 기준 서비스로 검증
    assertThatThrownBy(() -> currentService.validate(expiredResult))
        .isInstanceOf(ExpiredException.class);
}
```

### 적용 시나리오
- 시간 의존적 로직 테스트 (만료, 스케줄링, 타임스탬프)
- 외부 API 호출 테스트 (추상화 인터페이스 사용)
- 랜덤 값 테스트 (Random 추상화)

### 금지 패턴
```java
// ❌ 나쁜 예: 테스트용 별도 구현
private SomeObject createExpiredObject() {
    // 실제 구현과 다른 로직 - 신뢰성 저하 위험
}

// ✅ 좋은 예: 의존성 추상화
public SomeService(Clock clock, RandomGenerator random) {
    // 실제 구현을 그대로 사용, 의존성만 주입으로 제어
}
```

## 통합 테스트 코드 규칙
- 가독성과 유지 보수성을 고려
- 테스트 데이터 생성은 Fixture Factory 패턴 적용
- TestContainer 사용하여 실제 시나리오의 테스트 작성
- 외부 API 호출은 Mocking 처리
- 중복 코드 최소화

## 의존성 규칙
- ✅ Bootstrap → Core, Infrastructure, Common
- ✅ Infrastructure → Core, Common
- ✅ Core → Common
- ❌ Core 모듈 간 직접 의존 금지
- ❌ Common → 다른 모듈 의존 금지

## 커밋 규칙
- push 하기 전에 수정된 모듈의 테스트 코드가 모두 통과해야 함
- 실패하는 테스트가 있다면 모두 수정 후 push
- 테스트 커버리지 70% 이상 유지 (현재: 72%)


## 테스트 현황

### 테스트 커버리지 (현재: 72%)
- **전체 테스트 수**: 200+ 테스트
- **프로필 도메인**: 83% 커버리지
- **계정 도메인**: 69% 커버리지
- **JWT 도메인**: 59% 커버리지
- **도메인 서비스**: 0% (미구현)
- **도메인 이벤트**: 20% → 100% (개선됨)

### 주요 개선 사항
- `MarketingConsent`: 98% 커버리지 달성
- `NotificationSettings`: 97% 커버리지 달성
- `BrandPreference`: 100% 커버리지 달성
- 도메인 이벤트 테스트 완전 구현
- 값 객체 테스트 강화

### 테스트 파일 구조
```
src/test/java/
├── domain/
│   ├── event/             # 도메인 이벤트 테스트
│   │   ├── AccountCreatedEventTest.java
│   │   └── ... (기타 이벤트 테스트)
│   └── model/
│       ├── profile/       # 프로필 도메인 테스트
│       │   ├── MarketingConsentTest.java
│       │   ├── NotificationSettingsTest.java
│       │   ├── BrandPreferenceTest.java
│       │   └── ... (기타 프로필 테스트)
│       └── ... (기타 도메인 테스트)
└── ...
```

## 최근 완료된 작업 (2024년)

### PR 리뷰 반영 및 코드 개선
1. **Address.java**: 지번 주소 필드 추가 (한국 주소 체계 지원)
2. **AddressId.java & ProfileId.java**: String UUID → Long 타입 변경 (성능 개선)
3. **ContactInfo.java**: 긴급 연락처 제거로 설계 단순화
4. **PersonalInfo.java**: 생년월일/성별 선택적 필드로 변경 (개인정보 보호)

### 테스트 커버리지 대폭 개선
- **기존**: 62% → **현재**: 72% (10% 포인트 향상)
- 누락된 테스트 케이스 200+ 추가
- 모든 도메인 이벤트 테스트 완성
- 비즈니스 로직 검증 강화

### 아키텍처 문서화
- README.md에 헥사고날 아키텍처 다이어그램 추가
- 도메인 모델 구조도 추가
- 프로젝트 구조 상세화

## GitHub 작업 설정
- **GitHub CLI 사용**: 모든 GitHub 관련 작업은 `gh` 로 수행

## 개발 팁
- 새 모듈 추가 시 settings.gradle에 include 추가
- 각 core 모듈은 독립적으로 테스트 가능하도록 설계
- 도메인 이벤트는 다른 도메인과의 통신 수단으로 활용
- 기존 코드 변경 시 항상 테스트 코드도 변경
- 테스트 커버리지 리포트: `core/customer-core/build/reports/jacoco/test/html/index.html`