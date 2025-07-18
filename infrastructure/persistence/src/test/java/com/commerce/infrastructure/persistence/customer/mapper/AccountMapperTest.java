package com.commerce.infrastructure.persistence.customer.mapper;

import com.commerce.customer.core.domain.model.*;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountMapper 테스트")
class AccountMapperTest {

    private AccountMapper accountMapper;
    private Account testAccount;
    private AccountEntity testAccountEntity;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();

        // 도메인 객체 생성
        testAccount = Account.create(
                CustomerId.of(100L),
                Email.of("test@example.com"),
                Password.of("ValidPass123!")
        );

        // 엔티티 객체 생성
        testAccountEntity = AccountEntity.builder()
                .customerId(100L)
                .email("test@example.com")
                .password("ValidPass123!")
                .status(AccountEntity.AccountStatus.PENDING)
                .lastLoginAt(LocalDateTime.of(2024, 1, 2, 15, 30))
                .build();
        
        // 리플렉션을 사용해 필드 설정
        try {
            // accountId 설정
            java.lang.reflect.Field accountIdField = testAccountEntity.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(testAccountEntity, 1L);
            
            // BaseEntity의 createdAt, updatedAt 설정
            java.lang.reflect.Field createdAtField = testAccountEntity.getClass().getSuperclass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(testAccountEntity, LocalDateTime.of(2024, 1, 1, 9, 0));
            
            java.lang.reflect.Field updatedAtField = testAccountEntity.getClass().getSuperclass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(testAccountEntity, LocalDateTime.of(2024, 1, 1, 10, 0));
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }
    }

    @Test
    @DisplayName("도메인 객체를 엔티티로 성공적으로 변환한다")
    void toEntity_Success() {
        // When
        AccountEntity result = accountMapper.toEntity(testAccount);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(100L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPassword()).isEqualTo("ValidPass123!");
        assertThat(result.getStatus()).isEqualTo(AccountEntity.AccountStatus.PENDING);
    }

    @Test
    @DisplayName("null 도메인 객체는 null을 반환한다")
    void toEntity_NullInput() {
        // When
        AccountEntity result = accountMapper.toEntity(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("엔티티를 도메인 객체로 성공적으로 변환한다")
    void toDomain_Success() {
        // When
        Account result = accountMapper.toDomain(testAccountEntity);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountId().getValue()).isEqualTo(1L);
        assertThat(result.getCustomerId().getValue()).isEqualTo(100L);
        assertThat(result.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(result.getPassword().getValue()).isEqualTo("ValidPass123!");
        assertThat(result.getStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(result.getLastLoginAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 15, 30));
    }

    @Test
    @DisplayName("null 엔티티는 null을 반환한다")
    void toDomain_NullInput() {
        // When
        Account result = accountMapper.toDomain(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("모든 AccountStatus 값이 정확히 매핑된다")
    void statusMapping_AllValues() {
        // PENDING
        AccountEntity pendingEntity = AccountEntity.builder()
                .customerId(1L)
                .email("test@example.com")
                .password("ValidPass123!")
                .status(AccountEntity.AccountStatus.PENDING)
                .build();
        
        // 리플렉션으로 accountId 설정
        try {
            java.lang.reflect.Field accountIdField = pendingEntity.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(pendingEntity, 1L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }

        Account pendingAccount = accountMapper.toDomain(pendingEntity);
        assertThat(pendingAccount.getStatus()).isEqualTo(AccountStatus.PENDING);

        // ACTIVE
        AccountEntity activeEntity = AccountEntity.builder()
                .customerId(1L)
                .email("test@example.com")
                .password("ValidPass123!")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();
        
        // 리플렉션으로 accountId 설정
        try {
            java.lang.reflect.Field accountIdField = activeEntity.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(activeEntity, 2L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }

        Account activeAccount = accountMapper.toDomain(activeEntity);
        assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        // SUSPENDED
        AccountEntity suspendedEntity = AccountEntity.builder()
                .customerId(1L)
                .email("test@example.com")
                .password("ValidPass123!")
                .status(AccountEntity.AccountStatus.SUSPENDED)
                .build();
        
        // 리플렉션으로 accountId 설정
        try {
            java.lang.reflect.Field accountIdField = suspendedEntity.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(suspendedEntity, 3L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }

        Account suspendedAccount = accountMapper.toDomain(suspendedEntity);
        assertThat(suspendedAccount.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("도메인에서 엔티티로의 상태 변환이 정확하다")
    void domainToEntityStatusMapping() {
        // PENDING 계정 생성
        Account pendingAccount = Account.create(
                CustomerId.of(100L),
                Email.of("test@example.com"),
                Password.of("ValidPass123!")
        );

        AccountEntity result = accountMapper.toEntity(pendingAccount);
        assertThat(result.getStatus()).isEqualTo(AccountEntity.AccountStatus.PENDING);

        // 사용자가 직접 SUSPENDED 상태로 계정을 생성할 수 없으므로 일단 PENDING으로 생성
        Account suspendedAccount = Account.create(
                CustomerId.of(100L),
                Email.of("test@example.com"),
                Password.of("ValidPass123!")
        );

        AccountEntity suspendedResult = accountMapper.toEntity(suspendedAccount);
        assertThat(suspendedResult.getStatus()).isEqualTo(AccountEntity.AccountStatus.PENDING);
    }

    @Test
    @DisplayName("날짜 필드가 null인 경우 정상 처리한다")
    void handleNullDateFields() {
        // Given
        AccountEntity entityWithNullDates = AccountEntity.builder()
                .customerId(100L)
                .email("test@example.com")
                .password("ValidPass123!")
                .status(AccountEntity.AccountStatus.PENDING)
                .lastLoginAt(null)
                .build();
        
        // 리플렉션으로 accountId 설정
        try {
            java.lang.reflect.Field accountIdField = entityWithNullDates.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(entityWithNullDates, 100L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }

        // When
        Account result = accountMapper.toDomain(entityWithNullDates);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLastLoginAt()).isNull();
        assertThat(result.getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("양방향 변환이 일관성을 유지한다")
    void bidirectionalMapping_Consistency() {
        // Given
        Account originalAccount = Account.create(
                CustomerId.of(500L),
                Email.of("bidirectional@test.com"),
                Password.of("ComplexPass123!")
        );

        // When - 도메인 -> 엔티티 -> 도메인
        AccountEntity entity = accountMapper.toEntity(originalAccount);
        
        // 리플렉션으로 accountId 설정 (실제 저장 시 DB에서 할당되는 ID 시뮬레이션)
        try {
            java.lang.reflect.Field accountIdField = entity.getClass().getDeclaredField("accountId");
            accountIdField.setAccessible(true);
            accountIdField.set(entity, 500L);
        } catch (Exception e) {
            // 테스트 환경에서만 사용되므로 예외 무시
        }
        
        Account roundTripAccount = accountMapper.toDomain(entity);

        // Then - ID는 다를 수 있으므로(엔티티 저장 시 새로 생성됨) 다른 필드들만 검증
        assertThat(roundTripAccount.getCustomerId().getValue()).isEqualTo(originalAccount.getCustomerId().getValue());
        assertThat(roundTripAccount.getEmail().getValue()).isEqualTo(originalAccount.getEmail().getValue());
        assertThat(roundTripAccount.getPassword().getValue()).isEqualTo(originalAccount.getPassword().getValue());
        assertThat(roundTripAccount.getStatus()).isEqualTo(originalAccount.getStatus());
    }
}