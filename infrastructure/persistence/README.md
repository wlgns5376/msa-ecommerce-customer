# Infrastructure Persistence Module

## 개요
- **모듈명**: infrastructure/persistence
- **목적**: 도메인 객체의 데이터베이스 영속성 처리
- **아키텍처**: 헥사고날 아키텍처의 Secondary Adapter (출력 어댑터)
- **패턴**: Repository 패턴, JPA/Hibernate 사용
- **테스트**: 149개 테스트 전체 통과 (Mock + 실제 DB 통합)

## 구조 분석

### 📁 패키지 구조
```
src/main/java/com/commerce/infrastructure/persistence/
├── common/                     # 공통 기능
│   ├── BaseEntity.java        # 기본 엔티티 (JPA Auditing)
│   └── converter/             # JPA 컨버터
│       └── ProductStatusConverter.java
└── config/                    # 설정
    └── JpaConfig.java        # JPA 설정 (@EnableJpaAuditing)
```


## 🔧 기술 스택

### 의존성 (build.gradle)
```groovy
dependencies {
    // 프로젝트 의존성
    implementation project(':common')
    implementation project(':customer-core')
    
    // Spring Boot & JPA
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // QueryDSL
    implementation 'com.querydsl:querydsl-jpa:5.0.0:jakarta'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
    
    // 데이터베이스
    runtimeOnly 'com.h2database:h2'              // 개발용
    runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'  // 운영용
    
    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-testcontainers'
    testImplementation 'org.testcontainers:mariadb'
}
```

### 데이터베이스 설정
- **개발**: H2 In-Memory Database
- **운영**: MariaDB
- **테스트**: Testcontainers + MariaDB

## 🌟 주요 특징

### 1. 헥사고날 아키텍처 적용
- 도메인 계층과 인프라 계층의 완전한 분리
- 포트(Port) 인터페이스를 통한 의존성 역전
- 매퍼를 통한 도메인-엔티티 변환

### 2. 성능 최적화
- **QueryDSL 도입**: JPA N+1 문제 해결 및 복잡한 쿼리 최적화
- **Lazy Loading**: 필요시점 데이터 로딩
- **페이징 지원**: 대용량 데이터 처리
- **벌크 연산**: 여러 ID 조회/삭제 성능 향상
- **조인 최적화**: fetch join을 통한 연관 데이터 한번에 조회

