package com.commerce.customer.core.application.service;

import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.Password;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import com.commerce.customer.core.domain.model.jwt.TokenPair;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.customer.core.domain.service.AccountDomainService;
import com.commerce.customer.core.domain.service.PasswordEncoder;
import com.commerce.customer.core.domain.service.jwt.JwtTokenService;
import com.commerce.customer.core.domain.event.DomainEventPublisher;
import com.commerce.customer.core.domain.event.AccountCreatedEvent;
import com.commerce.customer.core.domain.event.AccountActivatedEvent;
import com.commerce.customer.core.application.usecase.account.ActivateAccountUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountApplicationService 테스트")
class AccountApplicationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountDomainService accountDomainService;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private AccountApplicationService accountApplicationService;

    private Email email;
    private Password password;
    private Account account;
    private AccountId accountId;
    private CustomerId customerId;

    @BeforeEach
    void setUp() {
        email = Email.of("test@example.com");
        password = Password.of("password123!");
        accountId = AccountId.of(1L);
        customerId = CustomerId.of(12345L);
        account = mock(Account.class);
    }

    @Test
    @DisplayName("계정 생성 성공")
    void createAccount_Success() {
        // given
        String activationCode = "ACTIVATION123";
        AccountCreatedEvent event = new AccountCreatedEvent(accountId, customerId, email, activationCode);
        given(accountRepository.generateCustomerId()).willReturn(customerId);
        given(accountDomainService.createAccount(any(CustomerId.class), any(Email.class), any(Password.class), any(PasswordEncoder.class)))
                .willReturn(account);
        given(account.getAccountId()).willReturn(accountId);
        given(account.getDomainEvents()).willReturn(List.of(event));

        // when
        AccountId result = accountApplicationService.createAccount(email, password);

        // then
        assertThat(result).isEqualTo(accountId);
        then(accountRepository).should().generateCustomerId();
        then(accountDomainService).should().createAccount(any(CustomerId.class), any(Email.class), any(Password.class), any(PasswordEncoder.class));
        then(account).should().raiseAccountCreatedEvent();
        then(domainEventPublisher).should().publishAccountCreatedEvent(event);
        then(account).should().clearDomainEvents();
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        AccountDomainService.LoginResult loginResult = AccountDomainService.LoginResult.success(accountId, customerId);
        TokenPair tokenPair = createTokenPair();
        
        given(accountDomainService.attemptLogin(any(Email.class), any(Password.class), any(PasswordEncoder.class)))
                .willReturn(loginResult);
        given(jwtTokenService.generateTokenPair(any(CustomerId.class), any(AccountId.class), any(Email.class)))
                .willReturn(tokenPair);

        // when
        TokenPair result = accountApplicationService.login(email, password);

        // then
        assertThat(result).isEqualTo(tokenPair);
        then(accountDomainService).should().attemptLogin(any(Email.class), any(Password.class), any(PasswordEncoder.class));
        then(jwtTokenService).should().generateTokenPair(customerId, accountId, email);
    }

    @Test
    @DisplayName("로그인 실패 - 로그인 결과가 실패인 경우")
    void login_Fail_LoginResultFailed() {
        // given
        AccountDomainService.LoginResult loginResult = AccountDomainService.LoginResult.wrongPassword(accountId);
        
        given(accountDomainService.attemptLogin(any(Email.class), any(Password.class), any(PasswordEncoder.class)))
                .willReturn(loginResult);

        // when & then
        assertThatThrownBy(() -> accountApplicationService.login(email, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // given
        String accessToken = "validAccessToken";
        JwtToken jwtToken = createJwtToken(accessToken, JwtTokenType.ACCESS);
        
        given(jwtTokenService.parseToken(accessToken)).willReturn(Optional.of(jwtToken));

        // when
        accountApplicationService.logout(accessToken);

        // then
        then(jwtTokenService).should().parseToken(accessToken);
        then(jwtTokenService).should().invalidateToken(jwtToken);
    }

    @Test
    @DisplayName("로그아웃 - 유효하지 않은 토큰")
    void logout_InvalidToken() {
        // given
        String accessToken = "invalidAccessToken";
        
        given(jwtTokenService.parseToken(accessToken)).willReturn(Optional.empty());

        // when
        accountApplicationService.logout(accessToken);

        // then
        then(jwtTokenService).should().parseToken(accessToken);
        then(jwtTokenService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() {
        // given
        String refreshTokenString = "validRefreshToken";
        JwtToken refreshJwtToken = createJwtToken(refreshTokenString, JwtTokenType.REFRESH);
        JwtToken newAccessToken = createJwtToken("newAccessToken", JwtTokenType.ACCESS);
        TokenPair expectedTokenPair = TokenPair.of(newAccessToken, refreshJwtToken);
        
        given(jwtTokenService.parseToken(refreshTokenString)).willReturn(Optional.of(refreshJwtToken));
        given(jwtTokenService.refreshAccessToken(refreshJwtToken)).willReturn(newAccessToken);

        // when
        TokenPair result = accountApplicationService.refreshToken(refreshTokenString);

        // then
        assertThat(result.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(result.getRefreshToken()).isEqualTo(refreshJwtToken);
        then(jwtTokenService).should().parseToken(refreshTokenString);
        then(jwtTokenService).should().refreshAccessToken(refreshJwtToken);
    }

    @Test
    @DisplayName("토큰 갱신 실패 - 유효하지 않은 리프레시 토큰")
    void refreshToken_InvalidRefreshToken() {
        // given
        String refreshTokenString = "invalidRefreshToken";
        
        given(jwtTokenService.parseToken(refreshTokenString)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountApplicationService.refreshToken(refreshTokenString))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 리프레시 토큰입니다.");
    }

    @Test
    @DisplayName("계정 조회 성공")
    void getAccount_Success() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));

        // when
        Account result = accountApplicationService.getAccount(accountId);

        // then
        assertThat(result).isEqualTo(account);
        then(accountRepository).should().findById(accountId);
    }

    @Test
    @DisplayName("계정 조회 실패 - 존재하지 않는 계정")
    void getAccount_NotFound() {
        // given
        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountApplicationService.getAccount(accountId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정을 찾을 수 없습니다.");
    }
    
    @Test
    @DisplayName("이메일로 계정 조회 성공")
    void getAccountByEmail_Success() {
        // given
        given(accountRepository.findByEmail(email)).willReturn(Optional.of(account));

        // when
        Account result = accountApplicationService.getAccountByEmail(email);

        // then
        assertThat(result).isEqualTo(account);
        then(accountRepository).should().findByEmail(email);
    }

    @Test
    @DisplayName("이메일로 계정 조회 실패 - 존재하지 않는 계정")
    void getAccountByEmail_NotFound() {
        // given
        given(accountRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountApplicationService.getAccountByEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정을 찾을 수 없습니다.");
    }
    
    @Test
    @DisplayName("계정 활성화 성공")
    void activateAccount_Success() {
        // given
        String activationCode = "ACTIVATION123";
        ActivateAccountUseCase useCase = new ActivateAccountUseCase(accountId, activationCode);
        AccountActivatedEvent event = AccountActivatedEvent.of(accountId, customerId);
        
        given(accountRepository.findById(accountId)).willReturn(Optional.of(account));
        given(account.getDomainEvents()).willReturn(List.of(event));

        // when
        accountApplicationService.activateAccount(useCase);

        // then
        then(account).should().activate(activationCode);
        then(accountRepository).should().save(account);
        then(domainEventPublisher).should().publishAccountActivatedEvent(event);
        then(account).should().clearDomainEvents();
    }
    
    @Test
    @DisplayName("계정 활성화 실패 - 존재하지 않는 계정")
    void activateAccount_NotFound() {
        // given
        ActivateAccountUseCase useCase = new ActivateAccountUseCase(accountId, "any-activation-code");
        
        given(accountRepository.findById(accountId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> accountApplicationService.activateAccount(useCase))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("계정을 찾을 수 없습니다.");
    }

    private TokenPair createTokenPair() {
        JwtToken accessToken = createJwtToken("accessToken", JwtTokenType.ACCESS);
        JwtToken refreshToken = createJwtToken("refreshToken", JwtTokenType.REFRESH);
        return TokenPair.of(accessToken, refreshToken);
    }

    private JwtToken createJwtToken(String value, JwtTokenType type) {
        LocalDateTime now = LocalDateTime.now();
        return JwtToken.of(value, type, now, now.plusMinutes(type.getExpirationMinutes()));
    }
}