# Claude Code Project Memory

## 공통
모든 답변은 한국어로 작성한다.

## 프로젝트 개요
- **프로젝트명**: 커머스 마이크로서비스 고객/인증 서비스 4계층 아키텍처
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
- **빌드 도구**: Gradle 8.5
- **아키텍처**: 4계층 아키텍처 (Bootstrap, Core, Infrastructure, Common)

## 프로젝트 구조
```

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

# 통합 테스트 (아직 없음)
./gradlew integrationTest
```

## 주요 기술 스택
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Lombok**: 코드 간소화 (모든 도메인 객체에 @Getter 적용)
- **JUnit 5**: 테스트 프레임워크
- **H2/PostgreSQL**: 데이터베이스
- **Redis**: 캐싱 및 세션 관리
- **Kafka**: 메시징 시스템

## 아키텍처 특징
1. **헥사고날 아키텍처**: 포트와 어댑터 패턴
2. **DDD**: 도메인 주도 설계
3. **멀티 카테고리**: 상품이 여러 카테고리에 속할 수 있음
4. **이벤트 기반**: 도메인 이벤트를 통한 느슨한 결합

## 코딩 규칙
1. **Lombok 사용**: 모든 엔티티/값객체에 @Getter 적용
2. **불변 객체**: 값 객체는 불변으로 설계
3. **유효성 검증**: 생성자에서 비즈니스 규칙 검증
4. **도메인 이벤트**: 중요한 상태 변경 시 이벤트 발행
5. **패키지 구조**: domain, application, usecase로 명확히 분리
6. **성능 최적화**: 쿼리 최적화, 인덱스 설정 등 고려, JPA N+1 고려, 필요 시 QueryDSL 사용

## 테스트 코드 규칙
- Given-When-Then 패턴 적용
- 예외 상황 테스트 포함
- DisplayName 사용하여 테스트 가독성 향상
- Parameterized Tests 를 적절히 활용

## 통합 테스트 코드 규칙
- 가독성과 유지 보수성을 고려
- 테스트 데이터 생성은 Fixture Factory 패턴 적용
- 중복 코드 최소화

## 의존성 규칙
- ✅ Bootstrap → Core, Infrastructure, Common
- ✅ Infrastructure → Core, Common
- ✅ Core → Common
- ❌ Core 모듈 간 직접 의존 금지
- ❌ Common → 다른 모듈 의존 금지

## 커밋 규칙
- push 하기 전에 수정된 모듈의 테스트 코드가 모두 통과해야 함


## 개발 팁
- 새 모듈 추가 시 settings.gradle에 include 추가
- 각 core 모듈은 독립적으로 테스트 가능하도록 설계
- 도메인 이벤트는 다른 도메인과의 통신 수단으로 활용
- 기존 코드 변경 시 항상 테스트 코드도 변경