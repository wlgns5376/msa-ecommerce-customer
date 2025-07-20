package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.ActivateAccountRequest;
import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("계정 컨트롤러 간단 통합테스트")
class SimpleAccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AccountJpaRepository accountRepository;
    
    private static final String TEST_PASSWORD = "Password123!";
    
    @BeforeEach
    void setUp() {
        waitForContainers();
    }
    
    @Test
    @DisplayName("계정 생성 API - 성공")
    void createAccount_Success() throws Exception {
        // Given
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest request = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.message").value("계정이 성공적으로 생성되었습니다."));
    }
    
    @Test
    @DisplayName("계정 생성 후 활성화 없이 로그인 시도 - 실패")
    void createAccountAndLogin_WithoutActivation_Failure() throws Exception {
        // Given - 계정 생성
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest createRequest = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
        
        // When - 로그인 (계정이 PENDING 상태여서 실패할 것)
        LoginRequest loginRequest = new LoginRequest(uniqueEmail, TEST_PASSWORD);
        
        // Then - 계정 상태로 인한 로그인 실패
        mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("계정 생성 후 활성화하고 로그인 - 성공")
    void createAccountActivateAndLogin_Success() throws Exception {
        // Given - 계정 생성
        String uniqueEmail = "active_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest createRequest = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        String createResponse = mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Long accountId = objectMapper.readTree(createResponse).get("accountId").asLong();
        
        // 활성화 코드 조회
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new AssertionError("계정을 찾을 수 없습니다."));
        String activationCode = accountEntity.getActivationCode();
        
        // 계정 활성화
        ActivateAccountRequest activateRequest = new ActivateAccountRequest(activationCode);
        
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isOk());
        
        // When - 활성화된 계정으로 로그인
        LoginRequest loginRequest = new LoginRequest(uniqueEmail, TEST_PASSWORD);
        
        // Then - 로그인 성공
        mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }
    
    @Test
    @DisplayName("계정 생성 API - 유효하지 않은 이메일 형식")
    void createAccount_InvalidEmail_Failure() throws Exception {
        // Given
        CreateAccountRequest request = new CreateAccountRequest("invalid-email", TEST_PASSWORD);
        
        // When & Then
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("로그인 API - 존재하지 않는 계정")
    void login_NonExistentAccount_Failure() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("nonexistent@example.com", TEST_PASSWORD);
        
        // When & Then - 존재하지 않는 계정은 BadRequest로 처리됨
        mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}