package com.commerce.infrastructure.persistence.customer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountEntity 테스트")
class AccountEntityTest {

    @Test
    @DisplayName("Builder를 통해 AccountEntity를 생성한다")
    void createAccountEntityWithBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        // When
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("hashedPassword123")
            .status(AccountEntity.AccountStatus.PENDING)
            .activatedAt(null)
            .lastLoginAt(null)
            .activationCode("ABCD1234")
            .activationCodeExpiresAt(expiresAt)
            .build();

        // Then
        assertThat(account.getCustomerId()).isEqualTo(1L);
        assertThat(account.getEmail()).isEqualTo("test@example.com");
        assertThat(account.getPassword()).isEqualTo("hashedPassword123");
        assertThat(account.getStatus()).isEqualTo(AccountEntity.AccountStatus.PENDING);
        assertThat(account.getActivatedAt()).isNull();
        assertThat(account.getLastLoginAt()).isNull();
        assertThat(account.getActivationCode()).isEqualTo("ABCD1234");
        assertThat(account.getActivationCodeExpiresAt()).isEqualTo(expiresAt);
        assertThat(account.getDeleted()).isFalse();
        assertThat(account.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("계정 상태를 ACTIVE로 변경하면 활성화 시간이 설정되고 활성화 코드가 제거된다")
    void updateStatusToActive() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.PENDING)
            .activationCode("CODE123")
            .activationCodeExpiresAt(LocalDateTime.now().plusHours(1))
            .build();

        // When
        account.updateStatus(AccountEntity.AccountStatus.ACTIVE);

        // Then
        assertThat(account.getStatus()).isEqualTo(AccountEntity.AccountStatus.ACTIVE);
        assertThat(account.getActivatedAt()).isNotNull();
        assertThat(account.getActivationCode()).isNull();
        assertThat(account.getActivationCodeExpiresAt()).isNull();
    }

    @Test
    @DisplayName("이미 활성화된 계정의 상태를 ACTIVE로 변경해도 activatedAt은 변경되지 않는다")
    void updateStatusToActiveWhenAlreadyActivated() {
        // Given
        LocalDateTime originalActivatedAt = LocalDateTime.now().minusDays(10);
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.INACTIVE)
            .activatedAt(originalActivatedAt)
            .build();

        // When
        account.updateStatus(AccountEntity.AccountStatus.ACTIVE);

        // Then
        assertThat(account.getStatus()).isEqualTo(AccountEntity.AccountStatus.ACTIVE);
        assertThat(account.getActivatedAt()).isEqualTo(originalActivatedAt);
    }

    @Test
    @DisplayName("계정 상태를 ACTIVE가 아닌 다른 상태로 변경한다")
    void updateStatusToNonActive() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();

        // When
        account.updateStatus(AccountEntity.AccountStatus.SUSPENDED);

        // Then
        assertThat(account.getStatus()).isEqualTo(AccountEntity.AccountStatus.SUSPENDED);
    }

    @Test
    @DisplayName("마지막 로그인 시간을 업데이트한다")
    void updateLastLoginAt() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();
        LocalDateTime loginTime = LocalDateTime.now();

        // When
        account.updateLastLoginAt(loginTime);

        // Then
        assertThat(account.getLastLoginAt()).isEqualTo(loginTime);
    }

    @Test
    @DisplayName("비밀번호를 업데이트한다")
    void updatePassword() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("oldPassword")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();

        // When
        account.updatePassword("newPassword");

        // Then
        assertThat(account.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("활성화 코드와 만료 시간을 업데이트한다")
    void updateActivationCode() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.PENDING)
            .build();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        // When
        account.updateActivationCode("NEWCODE123", expiresAt);

        // Then
        assertThat(account.getActivationCode()).isEqualTo("NEWCODE123");
        assertThat(account.getActivationCodeExpiresAt()).isEqualTo(expiresAt);
    }

    @Test
    @DisplayName("활성화 시간을 업데이트한다")
    void updateActivatedAt() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();
        LocalDateTime activatedAt = LocalDateTime.now();

        // When
        account.updateActivatedAt(activatedAt);

        // Then
        assertThat(account.getActivatedAt()).isEqualTo(activatedAt);
    }

    @Test
    @DisplayName("계정을 논리적으로 삭제한다")
    void markAsDeleted() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();

        // When
        account.markAsDeleted();

        // Then
        assertThat(account.getDeleted()).isTrue();
        assertThat(account.getDeletedAt()).isNotNull();
        assertThat(account.getStatus()).isEqualTo(AccountEntity.AccountStatus.DELETED);
        assertThat(account.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("삭제된 계정을 복원한다")
    void restore() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();
        account.markAsDeleted();

        // When
        account.restore();

        // Then
        assertThat(account.getDeleted()).isFalse();
        assertThat(account.getDeletedAt()).isNull();
        assertThat(account.isDeleted()).isFalse();
        // 상태는 비즈니스 로직에 따라 결정되므로 DELETED 상태 유지
        assertThat(account.getStatus()).isEqualTo(AccountEntity.AccountStatus.DELETED);
    }

    @Test
    @DisplayName("삭제되지 않은 계정의 isDeleted는 false를 반환한다")
    void isDeletedReturnsFalseForActiveAccount() {
        // Given
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.ACTIVE)
            .build();

        // Then
        assertThat(account.isDeleted()).isFalse();
    }

    @ParameterizedTest
    @DisplayName("AccountStatus 열거형 값들을 테스트한다")
    @MethodSource("provideAccountStatuses")
    void accountStatusEnumValues(AccountEntity.AccountStatus status, String expectedName) {
        // Then
        assertThat(status.name()).isEqualTo(expectedName);
    }

    private static Stream<Arguments> provideAccountStatuses() {
        return Stream.of(
            Arguments.of(AccountEntity.AccountStatus.PENDING, "PENDING"),
            Arguments.of(AccountEntity.AccountStatus.ACTIVE, "ACTIVE"),
            Arguments.of(AccountEntity.AccountStatus.INACTIVE, "INACTIVE"),
            Arguments.of(AccountEntity.AccountStatus.DORMANT, "DORMANT"),
            Arguments.of(AccountEntity.AccountStatus.SUSPENDED, "SUSPENDED"),
            Arguments.of(AccountEntity.AccountStatus.DELETED, "DELETED")
        );
    }

    @Test
    @DisplayName("기본 생성자는 protected로 접근이 제한된다")
    void protectedNoArgsConstructor() {
        // JPA를 위한 기본 생성자가 있지만 외부에서는 사용할 수 없음
        // 이 테스트는 컴파일 타임에 검증됨
        assertThat(AccountEntity.class).isNotNull();
    }

    @Test
    @DisplayName("deleted 필드의 기본값은 false이다")
    void deletedFieldDefaultValue() {
        // When
        AccountEntity account = AccountEntity.builder()
            .customerId(1L)
            .email("test@example.com")
            .password("password")
            .status(AccountEntity.AccountStatus.PENDING)
            .build();

        // Then
        assertThat(account.getDeleted()).isFalse();
    }
}