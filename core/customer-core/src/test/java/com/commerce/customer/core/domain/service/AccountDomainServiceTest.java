package com.commerce.customer.core.domain.service;

import com.commerce.customer.core.domain.model.*;
import com.commerce.customer.core.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountDomainService 테스트")
class AccountDomainServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountDomainService accountDomainService;

    private Email email;
    private Password rawPassword;
    private Password encodedPassword;
    private CustomerId customerId;
    private AccountId accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        email = Email.of("test@example.com");
        rawPassword = Password.of("password123!");
        encodedPassword = Password.ofEncoded("encodedPassword123!");
        customerId = CustomerId.of(12345L);
        accountId = AccountId.of(1L);
        account = mock(Account.class);
    }

    @Test
    @DisplayName("계정 생성 성공")
    void createAccount_Success() {
        // given
        given(accountRepository.existsByEmail(email)).willReturn(false);
        given(passwordEncoder.encode(rawPassword.getValue())).willReturn(encodedPassword.getValue());
        given(accountRepository.save(any(Account.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        Account result = accountDomainService.createAccount(customerId, email, rawPassword, passwordEncoder);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        then(accountRepository).should().existsByEmail(email);
        then(passwordEncoder).should().encode(rawPassword.getValue());
        then(accountRepository).should().save(any(Account.class));
    }

    @Test
    @DisplayName("계정 생성 실패 - 이메일 중복")
    void createAccount_EmailDuplicated() {
        // given
        given(accountRepository.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> accountDomainService.createAccount(customerId, email, rawPassword, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용 중인 이메일입니다: " + email.getValue());
        
        then(accountRepository).should().existsByEmail(email);
        then(accountRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("로그인 성공")
    void attemptLogin_Success() {
        // given
        given(account.isLocked()).willReturn(false);
        given(account.getStatus()).willReturn(AccountStatus.ACTIVE);
        given(account.getPassword()).willReturn(encodedPassword);
        given(account.getAccountId()).willReturn(accountId);
        given(account.getCustomerId()).willReturn(customerId);
        given(accountRepository.findByEmail(email)).willReturn(Optional.of(account));
        given(passwordEncoder.matches(rawPassword.getValue(), encodedPassword.getValue())).willReturn(true);
        given(accountRepository.save(account)).willReturn(account);

        // when
        AccountDomainService.LoginResult result = accountDomainService.attemptLogin(email, rawPassword, passwordEncoder);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getFailureReason()).isNull();
        then(account).should().recordSuccessfulLogin();
        then(accountRepository).should().save(account);
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 이메일")
    void attemptLogin_EmailNotFound() {
        // given
        given(accountRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountDomainService.attemptLogin(email, rawPassword, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 이메일입니다: " + email.getValue());
    }

    @Test
    @DisplayName("로그인 실패 - 계정 잠김")
    void attemptLogin_AccountLocked() {
        // given
        given(account.isLocked()).willReturn(true);
        given(account.getAccountId()).willReturn(accountId);
        given(accountRepository.findByEmail(email)).willReturn(Optional.of(account));

        // when
        AccountDomainService.LoginResult result = accountDomainService.attemptLogin(email, rawPassword, passwordEncoder);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getFailureReason()).isEqualTo("계정이 잠겨있습니다.");
        then(account).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("로그인 실패 - 유효하지 않은 계정 상태")
    void attemptLogin_InvalidStatus() {
        // given
        AccountStatus inactiveStatus = AccountStatus.INACTIVE;
        given(account.isLocked()).willReturn(false);
        given(account.getStatus()).willReturn(inactiveStatus);
        given(account.getAccountId()).willReturn(accountId);
        given(accountRepository.findByEmail(email)).willReturn(Optional.of(account));

        // when
        AccountDomainService.LoginResult result = accountDomainService.attemptLogin(email, rawPassword, passwordEncoder);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getFailureReason()).isEqualTo("로그인할 수 없는 계정 상태입니다.");
        assertThat(result.getAccountStatus()).isEqualTo(inactiveStatus);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void attemptLogin_WrongPassword() {
        // given
        given(account.isLocked()).willReturn(false);
        given(account.getStatus()).willReturn(AccountStatus.ACTIVE);
        given(account.getPassword()).willReturn(encodedPassword);
        given(account.getAccountId()).willReturn(accountId);
        given(accountRepository.findByEmail(email)).willReturn(Optional.of(account));
        given(passwordEncoder.matches(rawPassword.getValue(), encodedPassword.getValue())).willReturn(false);
        given(accountRepository.save(account)).willReturn(account);

        // when
        AccountDomainService.LoginResult result = accountDomainService.attemptLogin(email, rawPassword, passwordEncoder);

        // then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getAccountId()).isEqualTo(accountId);
        assertThat(result.getFailureReason()).isEqualTo("비밀번호가 일치하지 않습니다.");
        then(account).should().recordFailedLogin();
        then(accountRepository).should().save(account);
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // given
        Password newRawPassword = Password.of("newPassword123!");
        Password newEncodedPassword = Password.ofEncoded("encodedNewPassword123!");
        
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(account.getPassword()).willReturn(encodedPassword);
        given(passwordEncoder.matches(rawPassword.getValue(), encodedPassword.getValue())).willReturn(true);
        given(passwordEncoder.encode(newRawPassword.getValue())).willReturn(newEncodedPassword.getValue());
        given(accountRepository.save(account)).willReturn(account);

        // when
        accountDomainService.changePassword(accountId, rawPassword, newRawPassword, passwordEncoder);

        // then
        then(passwordEncoder).should().matches(rawPassword.getValue(), encodedPassword.getValue());
        then(passwordEncoder).should().encode(newRawPassword.getValue());
        then(account).should().changePassword(any(Password.class));
        then(accountRepository).should().save(account);
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 존재하지 않는 계정")
    void changePassword_AccountNotFound() {
        // given
        Password newPassword = Password.of("newPassword123!");
        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountDomainService.changePassword(accountId, rawPassword, newPassword, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 계정입니다: " + accountId.getValue());
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_WrongCurrentPassword() {
        // given
        Password newPassword = Password.of("newPassword123!");
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(account.getPassword()).willReturn(encodedPassword);
        given(passwordEncoder.matches(rawPassword.getValue(), encodedPassword.getValue())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> accountDomainService.changePassword(accountId, rawPassword, newPassword, passwordEncoder))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비밀번호가 일치하지 않습니다.");
        
        then(account).shouldHaveNoMoreInteractions();
        then(accountRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("계정 활성화 - deprecated 메서드 호출 시 예외 발생")
    void activateAccount_Deprecated() {
        // when & then
        assertThatThrownBy(() -> accountDomainService.activateAccount(accountId))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessage("계정 활성화는 인증 코드를 통해 수행되어야 합니다.");
    }
}