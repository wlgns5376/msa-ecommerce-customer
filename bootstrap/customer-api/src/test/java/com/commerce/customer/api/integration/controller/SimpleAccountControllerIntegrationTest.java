package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DisplayName("계정 컨트롤러 간단 통합테스트")
class SimpleAccountControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
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
    @DisplayName("계정 생성 후 로그인 API - 계정 상태로 인한 실패")
    void createAccountAndLogin_AccountStateFailure() throws Exception {
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