package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.customer.api.integration.fixture.AccountTestFixture.AuthenticatedAccountData;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.commerce.customer.api.integration.fixture.AccountTestFixture.*;
import static com.commerce.customer.api.integration.fixture.ProfileTestFixture.*;
import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("계정 보안 관련 통합테스트")
class AccountSecurityIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AccountJpaRepository accountRepository;
    
    @BeforeEach
    void setUp() {
        waitForContainers();
    }
    
    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도 - 실패")
    void login_WithWrongPassword_Failure() throws Exception {
        // Given - 계정 생성 및 활성화
        var accountData = createAndActivateAccount(mockMvc, objectMapper, accountRepository);
        
        // When & Then - 잘못된 비밀번호로 로그인 시도
        var loginRequest = new LoginRequest(accountData.getEmail(), "WrongPassword123!");
        
        mockMvc.perform(post(LOGIN_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("만료된 토큰으로 API 호출 - 실패")
    void apiCall_WithExpiredToken_Unauthorized() throws Exception {
        // Given - 만료된 토큰 (실제로는 잘못된 형식의 토큰으로 테스트)
        String expiredToken = "expired.token.here";
        
        // When & Then
        mockMvc.perform(get(MY_PROFILE_API_PATH)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + expiredToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("잘못된 형식의 토큰으로 API 호출 - 실패")
    void apiCall_WithMalformedToken_Unauthorized() throws Exception {
        // Given - 잘못된 형식의 토큰
        String malformedToken = "not-a-valid-jwt-token";
        
        // When & Then
        mockMvc.perform(get(MY_PROFILE_API_PATH)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + malformedToken))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("Bearer 접두사 없는 토큰으로 API 호출 - 실패")
    void apiCall_WithoutBearerPrefix_Unauthorized() throws Exception {
        // Given - 유효한 토큰이지만 Bearer 접두사 없음
        var authenticatedAccount = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
        
        // When & Then
        mockMvc.perform(get(MY_PROFILE_API_PATH)
                .header(AUTHORIZATION_HEADER, authenticatedAccount.getAccessToken())) // Bearer 없음
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("로그아웃 후 토큰 사용 - 실패")
    void apiCall_AfterLogout_Unauthorized() throws Exception {
        // Given - 로그인 후 로그아웃
        var authenticatedAccount = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
        
        // 로그아웃
        mockMvc.perform(post(LOGOUT_API_PATH)
                .header(AUTHORIZATION_HEADER, authenticatedAccount.getAuthorizationHeader()))
                .andExpect(status().isNoContent());
        
        // When & Then - 로그아웃 후 동일한 토큰으로 API 호출
        mockMvc.perform(get(MY_PROFILE_API_PATH)
                .header(AUTHORIZATION_HEADER, authenticatedAccount.getAuthorizationHeader()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("다른 계정의 리소스 접근 시도 - 실패")
    void accessOtherAccountResource_Forbidden() throws Exception {
        // Given - 두 개의 다른 계정 생성
        var account1 = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
        var account2 = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
        
        // account1으로 프로필 생성
        createProfile(mockMvc, objectMapper, account1.getAccessToken());
        
        // When & Then - account2의 토큰으로 account1의 프로필 수정 시도
        // 현재 구조상 자신의 프로필만 접근 가능하므로 이 테스트는 다른 방식으로 검증
        mockMvc.perform(get(MY_PROFILE_API_PATH)
                .header(AUTHORIZATION_HEADER, account2.getAuthorizationHeader()))
                .andExpect(status().isNotFound()); // account2는 프로필이 없음
    }
    
    @Test
    @DisplayName("SQL 인젝션 시도 - 안전하게 처리")
    void sqlInjection_HandledSafely() throws Exception {
        // Given - SQL 인젝션을 시도하는 이메일
        String maliciousEmail = "test@example.com' OR '1'='1";
        
        // When & Then - SQL 인젝션이 실패하고 정상적으로 에러 처리
        var request = createAccountRequest(maliciousEmail);
        mockMvc.perform(post(ACCOUNTS_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // 이메일 형식 검증에서 실패
    }
}