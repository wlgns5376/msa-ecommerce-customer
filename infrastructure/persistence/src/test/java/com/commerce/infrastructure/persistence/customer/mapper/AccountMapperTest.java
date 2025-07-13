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
                AccountId.of(1L),
                CustomerId.of(100L),
                Email.of("test@example.com"),
                Password.of("hashedPassword123"),
                AccountStatus.ACTIVE
        );

        // 엔티티 객체 생성
        testAccountEntity = AccountEntity.builder()
                .accountId(1L)
                .customerId(100L)
                .email("test@example.com")
                .password("hashedPassword123")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .activatedAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .lastLoginAt(LocalDateTime.of(2024, 1, 2, 15, 30))
                .build();
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
        assertThat(result.getPassword()).isEqualTo("hashedPassword123");
        assertThat(result.getStatus()).isEqualTo(AccountEntity.AccountStatus.ACTIVE);
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
        assertThat(result.getPassword().getValue()).isEqualTo("hashedPassword123");
        assertThat(result.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(result.getActivatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
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
                .password("password")
                .status(AccountEntity.AccountStatus.PENDING)
                .build();

        Account pendingAccount = accountMapper.toDomain(pendingEntity);
        assertThat(pendingAccount.getStatus()).isEqualTo(AccountStatus.PENDING);

        // ACTIVE
        AccountEntity activeEntity = AccountEntity.builder()
                .customerId(1L)
                .email("test@example.com")
                .password("password")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();

        Account activeAccount = accountMapper.toDomain(activeEntity);
        assertThat(activeAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);

        // SUSPENDED
        AccountEntity suspendedEntity = AccountEntity.builder()
                .customerId(1L)
                .email("test@example.com")
                .password("password")
                .status(AccountEntity.AccountStatus.SUSPENDED)
                .build();

        Account suspendedAccount = accountMapper.toDomain(suspendedEntity);
        assertThat(suspendedAccount.getStatus()).isEqualTo(AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("도메인에서 엔티티로의 상태 변환이 정확하다")
    void domainToEntityStatusMapping() {
        // PENDING 계정 생성
        Account pendingAccount = Account.create(
                AccountId.of(1L),
                CustomerId.of(100L),
                Email.of("test@example.com"),
                Password.of("password"),
                AccountStatus.PENDING
        );

        AccountEntity result = accountMapper.toEntity(pendingAccount);
        assertThat(result.getStatus()).isEqualTo(AccountEntity.AccountStatus.PENDING);

        // SUSPENDED 계정 생성
        Account suspendedAccount = Account.create(
                AccountId.of(1L),
                CustomerId.of(100L),
                Email.of("test@example.com"),
                Password.of("password"),
                AccountStatus.SUSPENDED
        );

        AccountEntity suspendedResult = accountMapper.toEntity(suspendedAccount);
        assertThat(suspendedResult.getStatus()).isEqualTo(AccountEntity.AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("날짜 필드가 null인 경우 정상 처리한다")
    void handleNullDateFields() {
        // Given
        AccountEntity entityWithNullDates = AccountEntity.builder()
                .accountId(1L)
                .customerId(100L)
                .email("test@example.com")
                .password("password")
                .status(AccountEntity.AccountStatus.PENDING)
                .activatedAt(null)
                .lastLoginAt(null)
                .build();

        // When
        Account result = accountMapper.toDomain(entityWithNullDates);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getActivatedAt()).isNull();
        assertThat(result.getLastLoginAt()).isNull();
    }

    @Test
    @DisplayName("양방향 변환이 일관성을 유지한다")
    void bidirectionalMapping_Consistency() {
        // Given
        Account originalAccount = Account.create(
                AccountId.of(5L),
                CustomerId.of(500L),
                Email.of("bidirectional@test.com"),
                Password.of("complexPassword123"),
                AccountStatus.ACTIVE
        );

        // When - 도메인 -> 엔티티 -> 도메인
        AccountEntity entity = accountMapper.toEntity(originalAccount);
        Account roundTripAccount = accountMapper.toDomain(entity);

        // Then
        assertThat(roundTripAccount.getAccountId().getValue()).isEqualTo(originalAccount.getAccountId().getValue());
        assertThat(roundTripAccount.getCustomerId().getValue()).isEqualTo(originalAccount.getCustomerId().getValue());
        assertThat(roundTripAccount.getEmail().getValue()).isEqualTo(originalAccount.getEmail().getValue());
        assertThat(roundTripAccount.getPassword().getValue()).isEqualTo(originalAccount.getPassword().getValue());
        assertThat(roundTripAccount.getStatus()).isEqualTo(originalAccount.getStatus());
    }
}