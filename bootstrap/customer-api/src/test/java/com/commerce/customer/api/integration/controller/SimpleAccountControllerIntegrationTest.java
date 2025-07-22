package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.customer.api.integration.fixture.AccountTestFixture;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.commerce.customer.api.dto.account.CreateAccountRequest;

import static com.commerce.customer.api.integration.fixture.AccountTestFixture.*;
import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("계정 컨트롤러 기본 통합테스트")
class SimpleAccountControllerIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("계정 생성 API - 성공")
    void createAccount_Success() throws Exception {
        // Given
        var request = createAccountRequest();
        
        // When & Then
        mockMvc.perform(post(ACCOUNTS_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.message").value(ACCOUNT_CREATED_MESSAGE));
    }
    
    
    
    @Test
    @DisplayName("계정 생성 API - 유효하지 않은 이메일 형식")
    void createAccount_InvalidEmail_Failure() throws Exception {
        // Given
        var request = createAccountRequest(INVALID_EMAIL);
        
        // When & Then
        mockMvc.perform(post(ACCOUNTS_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("로그인 API - 존재하지 않는 계정")
    void login_NonExistentAccount_Failure() throws Exception {
        // Given
        var loginRequest = createLoginRequest("nonexistent@example.com");
        
        // When & Then - 존재하지 않는 계정은 BadRequest로 처리됨
        mockMvc.perform(post(LOGIN_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("계정 생성 API - 약한 패스워드")
    void createAccount_WeakPassword_Failure() throws Exception {
        // Given
        var request = new CreateAccountRequest(generateUniqueEmail(), WEAK_PASSWORD);
        
        // When & Then
        mockMvc.perform(post(ACCOUNTS_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("계정 생성 API - 중복된 이메일")
    void createAccount_DuplicateEmail_Failure() throws Exception {
        // Given - 첫 번째 계정 생성
        String email = generateUniqueEmail("duplicate");
        createAccount(mockMvc, objectMapper, email);
        
        // When & Then - 같은 이메일로 두 번째 계정 생성 시도
        var request = createAccountRequest(email);
        mockMvc.perform(post(ACCOUNTS_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}