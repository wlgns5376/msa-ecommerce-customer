package com.commerce.customer.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Account 도메인 테스트")
class AccountTest {

    @Nested
    @DisplayName("계정 생성 테스트")
    class CreateAccountTest {

        @Test
        @DisplayName("유효한 정보로 계정을 생성할 수 있다")
        void createAccount_WithValidData_ShouldSuccess() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Email email = Email.of("test@example.com");
            Password password = Password.of("Test123!@#");

            // When
            Account account = Account.create(customerId, email, password);

            // Then
            assertThat(account.getAccountId()).isNotNull();
            assertThat(account.getCustomerId()).isEqualTo(customerId);
            assertThat(account.getEmail()).isEqualTo(email);
            assertThat(account.getPassword()).isEqualTo(password);
            assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);
            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getLoginFailCount()).isEqualTo(0);
            assertThat(account.isLocked()).isFalse();
        }

        @Test
        @DisplayName("null 값으로 계정 생성 시 예외가 발생한다")
        void createAccount_WithNullValues_ShouldThrowException() {
            // Given
            CustomerId customerId = CustomerId.generate();
            Email email = Email.of("test@example.com");
            Password password = Password.of("Test123!@#");

            // When & Then
            assertThatThrownBy(() -> Account.create(null, email, password))
                .isInstanceOf(NullPointerException.class);
            
            assertThatThrownBy(() -> Account.create(customerId, null, password))
                .isInstanceOf(NullPointerException.class);
            
            assertThatThrownBy(() -> Account.create(customerId, email, null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("계정 활성화 테스트")
    class ActivateAccountTest {

        @Test
        @DisplayName("PENDING 상태의 계정을 활성화할 수 있다")
        void activate_PendingAccount_ShouldSuccess() {
            // Given
            Account account = createTestAccount();

            // When
            account.activate();

            // Then
            assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        }

        @Test
        @DisplayName("ACTIVE 상태의 계정은 활성화할 수 없다")
        void activate_ActiveAccount_ShouldThrowException() {
            // Given
            Account account = createTestAccount();
            account.activate(); // 먼저 활성화

            // When & Then
            assertThatThrownBy(account::activate)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("현재 상태에서는 활성화할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("로그인 기록 테스트")
    class LoginRecordTest {

        @Test
        @DisplayName("성공적인 로그인을 기록할 수 있다")
        void recordSuccessfulLogin_WithActiveAccount_ShouldSuccess() {
            // Given
            Account account = createTestAccount();
            account.activate();

            // When
            account.recordSuccessfulLogin();

            // Then
            assertThat(account.getLastLoginAt()).isNotNull();
            assertThat(account.getLoginFailCount()).isEqualTo(0);
            assertThat(account.getLockedUntil()).isNull();
        }

        @Test
        @DisplayName("로그인 실패를 기록할 수 있다")
        void recordFailedLogin_ShouldIncreaseFailCount() {
            // Given
            Account account = createTestAccount();

            // When
            account.recordFailedLogin();

            // Then
            assertThat(account.getLoginFailCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("3회 로그인 실패 시 계정이 잠긴다")
        void recordFailedLogin_ThreeTimes_ShouldLockAccount() {
            // Given
            Account account = createTestAccount();

            // When
            account.recordFailedLogin();
            account.recordFailedLogin();
            account.recordFailedLogin();

            // Then
            assertThat(account.getLoginFailCount()).isEqualTo(3);
            assertThat(account.isLocked()).isTrue();
            assertThat(account.getLockedUntil()).isNotNull();
        }
    }

    @Nested
    @DisplayName("비밀번호 변경 테스트")
    class ChangePasswordTest {

        @Test
        @DisplayName("활성 계정의 비밀번호를 변경할 수 있다")
        void changePassword_WithActiveAccount_ShouldSuccess() {
            // Given
            Account account = createTestAccount();
            account.activate();
            Password newPassword = Password.of("NewPass123!@#");

            // When
            account.changePassword(newPassword);

            // Then
            assertThat(account.getPassword()).isEqualTo(newPassword);
        }

        @Test
        @DisplayName("비활성 계정의 비밀번호는 변경할 수 없다")
        void changePassword_WithInactiveAccount_ShouldThrowException() {
            // Given
            Account account = createTestAccount(); // PENDING 상태
            Password newPassword = Password.of("NewPass123!@#");

            // When & Then
            assertThatThrownBy(() -> account.changePassword(newPassword))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("활성 상태에서만 비밀번호를 변경할 수 있습니다");
        }
    }

    private Account createTestAccount() {
        CustomerId customerId = CustomerId.generate();
        Email email = Email.of("test@example.com");
        Password password = Password.of("Test123!@#");
        return Account.create(customerId, email, password);
    }
}