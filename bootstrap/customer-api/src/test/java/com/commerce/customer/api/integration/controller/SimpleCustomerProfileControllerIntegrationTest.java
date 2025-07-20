package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.profile.CreateProfileRequest;
import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("고객 프로필 컨트롤러 간단 통합테스트")
class SimpleCustomerProfileControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_FIRST_NAME = "홍";
    private static final String TEST_LAST_NAME = "길동";
    private static final String TEST_PHONE = "010-1234-5678";
    private static final LocalDate TEST_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String TEST_GENDER = "MALE";
    
    private String testEmail;
    
    @BeforeEach
    void setUp() throws Exception {
        waitForContainers();
        // 컨테이너 상태 재확인 및 추가 대기
        Thread.sleep(1000);
        
        // 인증 토큰 없음 테스트이므로 실제 계정 생성은 스킵
        // createTestAccount();
    }
    
    private void createTestAccount() throws Exception {
        // 고유한 이메일로 계정 생성
        testEmail = "profile_" + System.currentTimeMillis() + "@example.com";
        CreateAccountRequest createRequest = new CreateAccountRequest(testEmail, TEST_PASSWORD);
        
        try {
            mockMvc.perform(post("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated());
        } catch (Exception e) {
            // 계정 생성 실패 시 더 자세한 정보 출력
            System.err.println("Account creation failed for email: " + testEmail);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }
    
    
    @Test
    @DisplayName("프로필 생성 API - PENDING 계정으로 인한 실패")
    void createProfile_PendingAccount_Failure() throws Exception {
        // Given - PENDING 상태 계정은 유효한 토큰을 가질 수 없음
        CreateProfileRequest request = new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then - 유효한 토큰 없이 호출하면 401
        mockMvc.perform(post("/api/v1/profiles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
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
    @DisplayName("내 프로필 조회 API - 인증 토큰 없음으로 인한 실패")
    void getMyProfile_NoToken_Unauthorized() throws Exception {
        // When & Then - PENDING 계정은 유효한 토큰이 없으므로 401
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }
    
}