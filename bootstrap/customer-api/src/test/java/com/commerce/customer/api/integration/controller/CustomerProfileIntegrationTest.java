package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.account.ActivateAccountRequest;
import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.dto.profile.CreateProfileRequest;
import com.commerce.customer.api.dto.profile.UpdateProfileRequest;
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
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("고객 프로필 통합테스트 - 활성화된 계정 필요")
class CustomerProfileIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AccountJpaRepository accountRepository;
    
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_FIRST_NAME = "홍";
    private static final String TEST_LAST_NAME = "길동";
    private static final String TEST_PHONE = "010-1234-5678";
    private static final LocalDate TEST_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    private static final String TEST_GENDER = "MALE";
    
    private String accessToken;
    private Long accountId;
    
    @BeforeEach
    void setUp() throws Exception {
        waitForContainers();
        
        // 각 테스트마다 새로운 활성화된 계정 생성
        String uniqueEmail = "profile_test_" + System.currentTimeMillis() + "@example.com";
        
        // 1. 계정 생성
        CreateAccountRequest createRequest = new CreateAccountRequest(uniqueEmail, TEST_PASSWORD);
        
        String createResponse = mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        accountId = objectMapper.readTree(createResponse).get("accountId").asLong();
        
        // 2. 활성화 코드 조회
        AccountEntity accountEntity = accountRepository.findById(accountId)
                .orElseThrow(() -> new AssertionError("계정을 찾을 수 없습니다."));
        String activationCode = accountEntity.getActivationCode();
        
        // 3. 계정 활성화
        ActivateAccountRequest activateRequest = new ActivateAccountRequest(activationCode);
        
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(activateRequest)))
                .andExpect(status().isOk());
        
        // 4. 로그인하여 토큰 획득
        LoginRequest loginRequest = new LoginRequest(uniqueEmail, TEST_PASSWORD);
        
        String loginResponse = mockMvc.perform(post("/api/v1/accounts/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        accessToken = objectMapper.readTree(loginResponse).get("accessToken").asText();
    }
    
    @Test
    @DisplayName("프로필 생성 API - 활성화된 계정으로 성공")
    void createProfile_WithActivatedAccount_Success() throws Exception {
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
                .andDo(result -> {
                    System.out.println("Create Profile Response Status: " + result.getResponse().getStatus());
                    System.out.println("Create Profile Response Body: " + result.getResponse().getContentAsString());
                })
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").exists())
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 생성되었습니다."));
    }
    
    @Test
    @DisplayName("프로필 조회 API - 활성화된 계정으로 성공")
    void getMyProfile_WithActivatedAccount_Success() throws Exception {
        // Given - 프로필 생성
        CreateProfileRequest createRequest = new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        mockMvc.perform(post("/api/v1/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
        
        // When & Then - 프로필 조회
        mockMvc.perform(get("/api/v1/profiles/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.personalInfo.firstName").value(TEST_FIRST_NAME))
                .andExpect(jsonPath("$.personalInfo.lastName").value(TEST_LAST_NAME))
                .andExpect(jsonPath("$.personalInfo.birthDate").value(TEST_BIRTH_DATE.toString()))
                .andExpect(jsonPath("$.personalInfo.gender").value(TEST_GENDER))
                .andExpect(jsonPath("$.contactInfo.phoneNumber").value(TEST_PHONE));
    }
    
    @Test
    @DisplayName("프로필 수정 API - 활성화된 계정으로 성공")
    void updateProfile_WithActivatedAccount_Success() throws Exception {
        // Given - 프로필 생성
        CreateProfileRequest createRequest = new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        mockMvc.perform(post("/api/v1/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
        
        // When - 프로필 수정
        UpdateProfileRequest updateRequest = UpdateProfileRequest.builder()
                .phoneNumber("010-9876-5432")
                .build();
        
        // Then
        mockMvc.perform(patch("/api/v1/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필이 성공적으로 수정되었습니다."));
        
        // 수정 확인
        mockMvc.perform(get("/api/v1/profiles/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInfo.phoneNumber").value("010-9876-5432"));
    }
    
    @Test
    @DisplayName("프로필 생성 API - 인증 토큰 없이 실패")
    void createProfile_WithoutToken_Unauthorized() throws Exception {
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
    @DisplayName("프로필 조회 API - 인증 토큰 없이 실패")
    void getMyProfile_WithoutToken_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/profiles/me"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("프로필 중복 생성 시도 - 실패")
    void createDuplicateProfile_Failure() throws Exception {
        // Given - 첫 번째 프로필 생성
        CreateProfileRequest request = new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        mockMvc.perform(post("/api/v1/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
        
        // When & Then - 두 번째 프로필 생성 시도
        mockMvc.perform(post("/api/v1/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("프로필이 없는 상태에서 조회 - 404")
    void getMyProfile_WhenProfileNotExists_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/profiles/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}