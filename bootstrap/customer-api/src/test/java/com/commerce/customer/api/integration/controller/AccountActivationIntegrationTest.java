package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.ActivateAccountRequest;
import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.customer.core.domain.model.AccountId;
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
@DisplayName("계정 활성화 플로우 통합테스트")
class AccountActivationIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("계정 생성 -> 활성화 -> 로그인 -> 로그아웃 전체 시나리오")
    void accountLifecycleTest() throws Exception {
        // 1. 계정 생성
        String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest createRequest = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        String createResponse = mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").exists())
                .andExpect(jsonPath("$.message").value("계정이 성공적으로 생성되었습니다."))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Long accountId = objectMapper.readTree(createResponse).get("accountId").asLong();
        
        // 2. 생성된 계정에서 활성화 코드 조회 (실제 환경에서는 이메일로 전송됨)
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new AssertionError("계정을 찾을 수 없습니다."));
        String activationCode = accountEntity.getActivationCode();
        
        // 3. 계정 활성화
        ActivateAccountRequest activateRequest = new ActivateAccountRequest(activationCode);
        
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activated").value(true))
                .andExpect(jsonPath("$.message").value("계정이 성공적으로 활성화되었습니다."));
        
        // 4. 활성화된 계정으로 로그인
        LoginRequest loginRequest = new LoginRequest(uniqueEmail, TEST_PASSWORD);
        
        String loginResponse = mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        String accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();
        
        // 5. 로그아웃
        mockMvc.perform(post("/api/v1/accounts/logout")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("활성화되지 않은 계정으로 로그인 시도 - 실패")
    void loginWithPendingAccount_Failure() throws Exception {
        // Given - 계정 생성 (활성화하지 않음)
        String uniqueEmail = "pending_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest createRequest = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
        
        // When - 활성화하지 않은 계정으로 로그인 시도
        LoginRequest loginRequest = new LoginRequest(uniqueEmail, TEST_PASSWORD);
        
        // Then - 로그인 실패
        mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("잘못된 활성화 코드로 계정 활성화 시도 - 실패")
    void activateAccountWithInvalidCode_Failure() throws Exception {
        // Given - 계정 생성
        String uniqueEmail = "invalid_code_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest createRequest = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        String createResponse = mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        Long accountId = objectMapper.readTree(createResponse).get("accountId").asLong();
        
        // When - 잘못된 활성화 코드로 활성화 시도
        ActivateAccountRequest activateRequest = new ActivateAccountRequest("INVALID_CODE");
        
        // Then - 활성화 실패
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("이미 활성화된 계정을 다시 활성화 시도 - 실패")
    void activateAlreadyActivatedAccount_Failure() throws Exception {
        // Given - 계정 생성 및 활성화
        String uniqueEmail = "already_active_" + System.currentTimeMillis() + "@example.com";
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
        
        // 첫 번째 활성화 (성공)
        ActivateAccountRequest activateRequest = new ActivateAccountRequest(activationCode);
        
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isOk());
        
        // When - 두 번째 활성화 시도
        // Then - 활성화 실패
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isBadRequest());
    }
}