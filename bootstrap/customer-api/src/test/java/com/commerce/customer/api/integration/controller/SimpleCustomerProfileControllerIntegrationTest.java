package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.dto.account.LoginResponse;
import com.commerce.customer.api.dto.profile.CreateProfileRequest;
import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureWebMvc
@DisplayName("고객 프로필 컨트롤러 간단 통합테스트")
class SimpleCustomerProfileControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String TEST_EMAIL = "profile@example.com";
    private static final String TEST_PASSWORD = "password123!";
    private static final String TEST_FIRST_NAME = "홍";
    private static final String TEST_LAST_NAME = "길동";
    private static final String TEST_PHONE = "010-1234-5678";
    private static final LocalDate TEST_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String TEST_GENDER = "MALE";
    
    private String accessToken;
    
    @BeforeEach
    void setUp() throws Exception {
        waitForContainers();
        createAccountAndLogin();
    }
    
    private void createAccountAndLogin() throws Exception {
        // 계정 생성
        CreateAccountRequest createRequest = new CreateAccountRequest(TEST_EMAIL, TEST_PASSWORD);
        
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
        
        // 로그인
        LoginRequest loginRequest = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);
        
        String loginResponse = mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        LoginResponse response = objectMapper.readValue(loginResponse, LoginResponse.class);
        this.accessToken = response.getAccessToken();
    }
    
    @Test
    @DisplayName("프로필 생성 API - 성공")
    void createProfile_Success() throws Exception {
        // Given
        CreateProfileRequest request = new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").exists())
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 생성되었습니다."));
    }
    
    @Test
    @DisplayName("프로필 생성 API - 인증 토큰 없음")
    void createProfile_NoToken_Unauthorized() throws Exception {
        // Given
        CreateProfileRequest request = new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then
        mockMvc.perform(post("/api/v1/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("내 프로필 조회 API - 프로필 없음")
    void getMyProfile_ProfileNotFound_NotFound() throws Exception {
        // When & Then - 프로필 생성 없이 조회
        mockMvc.perform(get("/api/v1/profiles/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("내 프로필 조회 API - 인증 토큰 없음")
    void getMyProfile_NoToken_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }
}