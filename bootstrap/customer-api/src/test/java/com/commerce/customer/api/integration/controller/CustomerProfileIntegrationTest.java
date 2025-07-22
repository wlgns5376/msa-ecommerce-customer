package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.customer.api.integration.fixture.AccountTestFixture.AuthenticatedAccountData;
import com.commerce.customer.api.integration.fixture.ProfileTestFixture;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.annotation.DirtiesContext;

import static com.commerce.customer.api.integration.fixture.AccountTestFixture.*;
import static com.commerce.customer.api.integration.fixture.ProfileTestFixture.*;
import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static com.commerce.customer.api.integration.helper.MockMvcHelper.*;
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
    
    private AuthenticatedAccountData authenticatedAccount;
    
    @BeforeEach
    void setUp() throws Exception {
        waitForContainers();
        // 각 테스트마다 새로운 활성화된 계정 생성
        authenticatedAccount = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
    }
    
    @Test
    @DisplayName("프로필 생성 API - 활성화된 계정으로 성공")
    void createProfile_WithActivatedAccount_Success() throws Exception {
        // Given
        var request = createProfileRequest();
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH), 
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").exists())
                .andExpect(jsonPath("$.message").value(PROFILE_CREATED_MESSAGE));
    }
    
    @Test
    @DisplayName("프로필 조회 API - 활성화된 계정으로 성공")
    void getMyProfile_WithActivatedAccount_Success() throws Exception {
        // Given - 프로필 생성
        createProfile(mockMvc, objectMapper, authenticatedAccount.getAccessToken());
        
        // When & Then - 프로필 조회
        mockMvc.perform(authenticatedRequest(
                get(MY_PROFILE_API_PATH), 
                authenticatedAccount.getAccessToken()))
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
        createProfile(mockMvc, objectMapper, authenticatedAccount.getAccessToken());
        
        // When - 프로필 수정
        var updateRequest = updatePhoneNumberRequest(TEST_PHONE_UPDATED);
        
        // Then
        mockMvc.perform(authenticatedJsonRequest(
                patch(PROFILES_API_PATH),
                objectMapper.writeValueAsString(updateRequest),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(PROFILE_UPDATED_MESSAGE));
        
        // 수정 확인
        mockMvc.perform(authenticatedRequest(
                get(MY_PROFILE_API_PATH),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactInfo.phoneNumber").value(TEST_PHONE_UPDATED));
    }
    
    @Test
    @DisplayName("프로필 생성 API - 인증 토큰 없이 실패")
    void createProfile_WithoutToken_Unauthorized() throws Exception {
        // Given
        var request = createProfileRequest();
        
        // When & Then
        mockMvc.perform(jsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("프로필 조회 API - 인증 토큰 없이 실패")
    void getMyProfile_WithoutToken_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get(MY_PROFILE_API_PATH))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @DisplayName("프로필 중복 생성 시도 - 실패")
    void createDuplicateProfile_Failure() throws Exception {
        // Given - 첫 번째 프로필 생성
        var request = createProfileRequest();
        createProfile(mockMvc, objectMapper, authenticatedAccount.getAccessToken(), request);
        
        // When & Then - 두 번째 프로필 생성 시도
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("프로필이 없는 상태에서 조회 - 404")
    void getMyProfile_WhenProfileNotExists_NotFound() throws Exception {
        // When & Then
        mockMvc.perform(authenticatedRequest(
                get(MY_PROFILE_API_PATH),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isNotFound());
    }
}