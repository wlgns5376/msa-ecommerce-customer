# Customer Core 모듈 아키텍처

## 개요

Customer Core 모듈은 커머스 시스템의 고객 관리 및 인증을 담당하는 핵심 도메인 모듈입니다. 헥사고날 아키텍처와 DDD(Domain-Driven Design) 원칙을 따라 설계되었습니다.

## 아키텍처 개요

```mermaid
graph TB
    subgraph "Customer Core Module"
        subgraph "Application Layer"
            subgraph "Services"
                ACCOUNT_APP[AccountApplicationService]
                PROFILE_APP[CustomerProfileApplicationService]
            end
            subgraph "Use Cases"
                UC_CREATE[CreateAccountUseCase]
                UC_LOGIN[LoginUseCase]
                UC_LOGOUT[LogoutUseCase]
                UC_REFRESH[RefreshTokenUseCase]
                UC_PROFILE_CREATE[CreateCustomerProfileUseCase]
                UC_PROFILE_GET[GetCustomerProfileUseCase]
                UC_PROFILE_UPDATE[UpdateCustomerProfileUseCase]
            end
        end
        
        subgraph "Domain Layer"
            subgraph "Models"
                Account[Account]
                AccountId[AccountId]
                CustomerId[CustomerId]
                Email[Email]
                Password[Password]
                AccountStatus[AccountStatus]
                
                subgraph "JWT Models"
                    JwtToken[JwtToken]
                    JwtClaims[JwtClaims]
                    JwtTokenType[JwtTokenType]
                    TokenPair[TokenPair]
                end
            end
            
            subgraph "Services"
                AccountDomainService[AccountDomainService]
                JwtTokenService[JwtTokenService]
                JwtTokenServiceImpl[JwtTokenServiceImpl]
            end
            
            subgraph "Repositories"
                AccountRepository[AccountRepository]
            end
            
            subgraph "Events"
                AccountCreatedEvent[AccountCreatedEvent]
                AccountActivatedEvent[AccountActivatedEvent]
                LoginSuccessfulEvent[LoginSuccessfulEvent]
                TokenGeneratedEvent[TokenGeneratedEvent]
                TokenInvalidatedEvent[TokenInvalidatedEvent]
            end
            
            subgraph "Exceptions"
                JwtTokenException[JwtTokenException]
                ExpiredJwtTokenException[ExpiredJwtTokenException]
                InvalidJwtTokenException[InvalidJwtTokenException]
            end
        end
    end
    
    Account --> AccountId
    Account --> CustomerId
    Account --> Email
    Account --> Password
    Account --> AccountStatus
    
    AccountDomainService --> Account
    AccountDomainService --> AccountRepository
    AccountDomainService -.-> Events
    
    JwtTokenServiceImpl --> JwtToken
    JwtTokenServiceImpl --> JwtClaims
    JwtTokenServiceImpl --> TokenPair
    JwtTokenServiceImpl -.-> Exceptions
    
    ACCOUNT_APP --> UC_CREATE
    ACCOUNT_APP --> UC_LOGIN
    ACCOUNT_APP --> UC_LOGOUT
    ACCOUNT_APP --> UC_REFRESH
    PROFILE_APP --> UC_PROFILE_CREATE
    PROFILE_APP --> UC_PROFILE_GET
    PROFILE_APP --> UC_PROFILE_UPDATE
    
    ACCOUNT_APP --> AccountDomainService
    ACCOUNT_APP --> JwtTokenService
    PROFILE_APP --> CustomerProfileDomainService
```

## 도메인 모델 상세

### 핵심 엔티티

```mermaid
classDiagram
    class Account {
        -AccountId accountId
        -CustomerId customerId
        -Email email
        -Password password
        -AccountStatus status
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -LocalDateTime lastLoginAt
        -int loginFailCount
        -LocalDateTime lockedUntil
        +create(CustomerId, Email, Password)
        +activate()
        +deactivate()
        +changePassword(Password)
        +recordSuccessfulLogin()
        +recordFailedLogin()
        +isLocked()
        +delete()
    }
    
    class AccountId {
        -String value
        +generate()
        +of(String)
    }
    
    class CustomerId {
        -String value
        +generate()
        +of(String)
    }
    
    class Email {
        -String value
        +of(String)
        -validateFormat()
    }
    
    class Password {
        -String value
        +ofRaw(String)
        +ofEncoded(String)
        -validateStrength()
    }
    
    class AccountStatus {
        <<enumeration>>
        PENDING
        ACTIVE
        INACTIVE
        DORMANT
        SUSPENDED
        DELETED
        +canLogin()
        +canActivate()
        +canDeactivate()
        +canDelete()
    }
    
    Account --> AccountId
    Account --> CustomerId
    Account --> Email
    Account --> Password
    Account --> AccountStatus
```

### JWT 토큰 모델

```mermaid
classDiagram
    class JwtToken {
        -String value
        -JwtTokenType type
        -LocalDateTime issuedAt
        -LocalDateTime expiresAt
        +of(String, JwtTokenType, LocalDateTime, LocalDateTime)
        +isExpired()
        +isValid()
    }
    
    class JwtTokenType {
        <<enumeration>>
        ACCESS(15분)
        REFRESH(7일)
        +getExpirationMinutes()
    }
    
    class TokenPair {
        -JwtToken accessToken
        -JwtToken refreshToken
        +of(JwtToken, JwtToken)
    }
    
    class JwtClaims {
        -String subject
        -String accountId
        -String email
        -String issuer
        -String audience
        -LocalDateTime issuedAt
        -LocalDateTime expiresAt
        -JwtTokenType tokenType
        +getCustomerId()
        +getAccountIdObject()
        +getEmailObject()
    }
    
    JwtToken --> JwtTokenType
    TokenPair --> JwtToken
    JwtClaims --> JwtTokenType
```

## 서비스 레이어

```mermaid
graph TD
    subgraph "Domain Services"
        AccountDomainService --> AccountRepository
        AccountDomainService --> PasswordEncoder
        
        JwtTokenServiceImpl --> JwtParser
        JwtTokenServiceImpl --> SecretKey
        JwtTokenServiceImpl --> TokenBlacklist
    end
    
    subgraph "Service Operations"
        CreateAccount[계정 생성]
        Login[로그인 시도]
        ChangePassword[비밀번호 변경]
        ActivateAccount[계정 활성화]
        
        GenerateTokenPair[토큰 쌍 생성]
        RefreshToken[토큰 갱신]
        ValidateToken[토큰 검증]
        InvalidateToken[토큰 무효화]
    end
    
    AccountDomainService --> CreateAccount
    AccountDomainService --> Login
    AccountDomainService --> ChangePassword
    AccountDomainService --> ActivateAccount
    
    JwtTokenServiceImpl --> GenerateTokenPair
    JwtTokenServiceImpl --> RefreshToken
    JwtTokenServiceImpl --> ValidateToken
    JwtTokenServiceImpl --> InvalidateToken
```

## 비즈니스 플로우

### 계정 생성 플로우

```mermaid
sequenceDiagram
    participant Client
    participant AccountDomainService
    participant AccountRepository
    participant Account
    
    Client->>AccountDomainService: createAccount(customerId, email, password)
    AccountDomainService->>AccountRepository: existsByEmail(email)
    AccountRepository-->>AccountDomainService: false
    AccountDomainService->>Account: create(customerId, email, encodedPassword)
    Account-->>AccountDomainService: account (PENDING 상태)
    AccountDomainService->>AccountRepository: save(account)
    AccountRepository-->>AccountDomainService: saved account
    AccountDomainService-->>Client: account
```

### 로그인 플로우

```mermaid
sequenceDiagram
    participant Client
    participant AccountDomainService
    participant JwtTokenService
    participant AccountRepository
    participant Account
    
    Client->>AccountDomainService: attemptLogin(email, password)
    AccountDomainService->>AccountRepository: findByEmail(email)
    AccountRepository-->>AccountDomainService: account
    AccountDomainService->>Account: isLocked()
    Account-->>AccountDomainService: false
    AccountDomainService->>Account: getStatus().canLogin()
    Account-->>AccountDomainService: true
    AccountDomainService->>AccountDomainService: passwordEncoder.matches(password)
    AccountDomainService->>Account: recordSuccessfulLogin()
    AccountDomainService->>AccountRepository: save(account)
    AccountDomainService-->>Client: LoginResult.success
    
    Client->>JwtTokenService: generateTokenPair(customerId, accountId, email)
    JwtTokenService-->>Client: TokenPair(accessToken, refreshToken)
```

### 토큰 검증 플로우

```mermaid
sequenceDiagram
    participant Client
    participant JwtTokenService
    participant TokenBlacklist
    participant JwtParser
    
    Client->>JwtTokenService: validateToken(jwtToken)
    JwtTokenService->>TokenBlacklist: isTokenBlacklisted(token)
    TokenBlacklist-->>JwtTokenService: false
    JwtTokenService->>JwtParser: parseSignedClaims(token.value)
    JwtParser-->>JwtTokenService: claims
    JwtTokenService-->>Client: Optional<JwtClaims>
```

## 계정 상태 전이도

```mermaid
stateDiagram-v2
    [*] --> PENDING : 계정 생성
    
    PENDING --> ACTIVE : 이메일 인증 완료
    PENDING --> DELETED : 계정 삭제
    
    ACTIVE --> INACTIVE : 관리자 비활성화
    ACTIVE --> DORMANT : 장기간 미접속
    ACTIVE --> SUSPENDED : 정책 위반
    ACTIVE --> DELETED : 계정 삭제
    
    INACTIVE --> ACTIVE : 재활성화
    INACTIVE --> DELETED : 계정 삭제
    
    DORMANT --> ACTIVE : 재활성화
    DORMANT --> DELETED : 계정 삭제
    
    SUSPENDED --> ACTIVE : 정지 해제
    SUSPENDED --> DELETED : 계정 삭제
    
    DELETED --> [*] : 최종 상태
```

## 도메인 이벤트

```mermaid
graph LR
    subgraph "Account Events"
        AccountCreatedEvent[계정 생성됨]
        AccountActivatedEvent[계정 활성화됨]
        LoginSuccessfulEvent[로그인 성공]
    end
    
    subgraph "Token Events"
        TokenGeneratedEvent[토큰 생성됨]
        TokenInvalidatedEvent[토큰 무효화됨]
    end
    
    Account --> AccountCreatedEvent
    Account --> AccountActivatedEvent
    Account --> LoginSuccessfulEvent
    
    JwtTokenService --> TokenGeneratedEvent
    JwtTokenService --> TokenInvalidatedEvent
```

## 예외 처리

```mermaid
graph TD
    JwtTokenException --> ExpiredJwtTokenException
    JwtTokenException --> InvalidJwtTokenException
    
    subgraph "Business Exceptions"
        IllegalArgumentException[잘못된 인자]
        IllegalStateException[잘못된 상태]
    end
    
    subgraph "JWT Exceptions"
        ExpiredJwtTokenException[만료된 토큰]
        InvalidJwtTokenException[유효하지 않은 토큰]
    end
```

## 주요 설계 원칙

### 1. 도메인 모델의 불변성
- 모든 값 객체(Value Object)는 불변으로 설계
- 엔티티의 상태 변경은 도메인 메서드를 통해서만 가능

### 2. 비즈니스 규칙 캡슐화
- 계정 상태 전이 규칙을 AccountStatus enum에 캡슐화
- 비밀번호 강도 검증을 Password 값 객체에 캡슐화
- 이메일 형식 검증을 Email 값 객체에 캡슐화

### 3. 의존성 역전
- 도메인 서비스는 인터페이스를 통해 외부 의존성과 상호작용
- PasswordEncoder 인터페이스를 통한 암호화 서비스 추상화

### 4. 이벤트 기반 설계
- 도메인 이벤트를 통한 느슨한 결합
- 향후 이벤트 소싱 및 CQRS 패턴 적용 가능

### 5. 보안 고려사항
- JWT 토큰 블랙리스트 관리
- 로그인 실패 횟수 제한 및 계정 잠금
- 비밀번호 암호화 및 강도 검증

## 향후 확장 계획

1. **이벤트 발행 기능 완성**: 현재 주석 처리된 도메인 이벤트 발행 로직 구현
2. **소셜 로그인 지원**: OAuth2 제공자를 통한 로그인 기능 추가
3. **다중 인증 방식**: SMS, 이메일 OTP 등 추가 인증 방식 지원
4. **토큰 저장소 개선**: Redis 기반 토큰 블랙리스트 및 세션 관리
5. **감사 로그**: 계정 관련 모든 활동에 대한 감사 로그 기능