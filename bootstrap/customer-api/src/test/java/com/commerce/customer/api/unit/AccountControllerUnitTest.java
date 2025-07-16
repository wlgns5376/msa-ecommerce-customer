package com.commerce.customer.api.unit;

import com.commerce.customer.api.controller.AccountController;
import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.CreateAccountResponse;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.dto.account.LoginResponse;
import com.commerce.customer.api.dto.account.RefreshTokenRequest;
import com.commerce.customer.api.dto.account.RefreshTokenResponse;
import com.commerce.customer.core.application.service.AccountApplicationService;
import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import com.commerce.customer.core.domain.model.jwt.TokenPair;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountController 단위 테스트")
class AccountControllerUnitTest {

    @Mock
    private AccountApplicationService accountApplicationService;

    @InjectMocks
    private AccountController accountController;

    private CreateAccountRequest createAccountRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private AccountId accountId;
    private TokenPair tokenPair;
    private Account account;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        createAccountRequest = new CreateAccountRequest("test@example.com", "password123!");
        loginRequest = new LoginRequest("test@example.com", "password123!");
        refreshTokenRequest = new RefreshTokenRequest("refreshToken");
        accountId = AccountId.of(1L);
        tokenPair = createTokenPair();
        account = mock(Account.class);
        request = mock(HttpServletRequest.class);
    }

    @Test
    @DisplayName("계정 생성 성공")
    void createAccount_Success() {
        // given
        given(accountApplicationService.createAccount(any(), any())).willReturn(accountId);

        // when
        ResponseEntity<CreateAccountResponse> response = accountController.createAccount(createAccountRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getAccountId()).isEqualTo(1L);
        assertThat(response.getBody().getMessage()).isEqualTo("계정이 성공적으로 생성되었습니다.");
        then(accountApplicationService).should().createAccount(any(), any());
    }

    @Test
    @DisplayName("로그인 성공")
    void login_Success() {
        // given
        given(accountApplicationService.login(any(), any())).willReturn(tokenPair);

        // when
        ResponseEntity<LoginResponse> response = accountController.login(loginRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getBody().getRefreshToken()).isEqualTo("refreshToken");
        then(accountApplicationService).should().login(any(), any());
    }

    @Test
    @DisplayName("로그아웃 성공")
    void logout_Success() {
        // when
        given(request.getHeader("Authorization")).willReturn("Bearer accessToken");

        // when
        ResponseEntity<Void> response = accountController.logout(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(accountApplicationService).should().logout("accessToken");
    }

    @Test
    @DisplayName("토큰 갱신 성공")
    void refreshToken_Success() {
        // given
        given(accountApplicationService.refreshToken(any())).willReturn(tokenPair);

        // when
        ResponseEntity<RefreshTokenResponse> response = accountController.refreshToken(refreshTokenRequest);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getBody().getRefreshToken()).isEqualTo("refreshToken");
        then(accountApplicationService).should().refreshToken("refreshToken");
    }

    @Test
    @DisplayName("계정 정보 조회 성공")
    void getMyAccount_Success() {
        // given
        given(accountApplicationService.getAccount(any())).willReturn(account);

        // when
        given(request.getHeader("Authorization")).willReturn("Bearer accessToken");

        // when
        ResponseEntity<?> response = accountController.getMyAccount(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        then(accountApplicationService).should().getAccount(any());
    }

    private TokenPair createTokenPair() {
        LocalDateTime now = LocalDateTime.now();
        JwtToken accessToken = JwtToken.of("accessToken", JwtTokenType.ACCESS, now, now.plusMinutes(15));
        JwtToken refreshToken = JwtToken.of("refreshToken", JwtTokenType.REFRESH, now, now.plusDays(7));
        return TokenPair.of(accessToken, refreshToken);
    }
}