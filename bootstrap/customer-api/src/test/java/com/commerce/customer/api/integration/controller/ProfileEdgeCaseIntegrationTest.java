package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.dto.profile.UpdateProfileRequest;
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

import java.time.LocalDate;

import static com.commerce.customer.api.integration.fixture.AccountTestFixture.*;
import static com.commerce.customer.api.integration.fixture.ProfileTestFixture.*;
import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static com.commerce.customer.api.integration.helper.MockMvcHelper.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("프로필 엣지 케이스 통합테스트")
class ProfileEdgeCaseIntegrationTest extends AbstractIntegrationTest {

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
        authenticatedAccount = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
    }
    
    @Test
    @DisplayName("프로필 생성 - 최소 필수 정보만으로 생성")
    void createProfile_WithMinimalInfo_Success() throws Exception {
        // Given - 생년월일과 성별 없이 최소 정보만
        var request = createMinimalProfileRequest();
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.profileId").exists());
    }
    
    @Test
    @DisplayName("프로필 생성 - 미래 날짜 생년월일")
    void createProfile_WithFutureBirthDate_Failure() throws Exception {
        // Given - 미래 날짜로 생년월일 설정
        var request = createProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            LocalDate.now().plusDays(1),  // 미래 날짜
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("프로필 생성 - 너무 오래된 생년월일")
    void createProfile_WithTooOldBirthDate_Failure() throws Exception {
        // Given - 150년 이상 전 날짜
        var request = createProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            LocalDate.now().minusYears(151),
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("프로필 생성 - 잘못된 전화번호 형식")
    void createProfile_WithInvalidPhoneFormat_Failure() throws Exception {
        // Given - 잘못된 전화번호 형식들
        String[] invalidPhones = {
            "01012345678",     // 하이픈 없음
            "010-1234-567",    // 자릿수 부족
            "010-1234-56789",  // 자릿수 초과
            "011-1234-5678",   // 잘못된 번호 대역
            "phone-number",    // 문자열
            "010 1234 5678"    // 공백 구분
        };
        
        for (String invalidPhone : invalidPhones) {
            var request = createProfileRequest(
                TEST_FIRST_NAME,
                TEST_LAST_NAME,
                TEST_BIRTH_DATE,
                TEST_GENDER,
                invalidPhone
            );
            
            mockMvc.perform(authenticatedJsonRequest(
                    post(PROFILES_API_PATH),
                    objectMapper.writeValueAsString(request),
                    authenticatedAccount.getAccessToken()))
                    .andExpect(status().isBadRequest());
        }
    }
    
    @Test
    @DisplayName("프로필 생성 - 잘못된 성별 값")
    void createProfile_WithInvalidGender_Failure() throws Exception {
        // Given - 잘못된 성별 값
        var request = createProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            "UNKNOWN",  // 허용되지 않는 값
            TEST_PHONE
        );
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("프로필 수정 - 빈 업데이트 요청")
    void updateProfile_WithEmptyRequest_Success() throws Exception {
        // Given - 프로필 생성
        createProfile(mockMvc, objectMapper, authenticatedAccount.getAccessToken());
        
        // 빈 업데이트 요청
        var updateRequest = UpdateProfileRequest.builder().build();
        
        // When & Then - 아무것도 변경하지 않아도 성공
        mockMvc.perform(authenticatedJsonRequest(
                patch(PROFILES_API_PATH),
                objectMapper.writeValueAsString(updateRequest),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isOk());
    }
    
    @Test
    @DisplayName("프로필 수정 - 존재하지 않는 프로필")
    void updateProfile_WhenNotExists_NotFound() throws Exception {
        // Given - 프로필 생성하지 않음
        var updateRequest = updatePhoneNumberRequest(TEST_PHONE_UPDATED);
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                patch(PROFILES_API_PATH),
                objectMapper.writeValueAsString(updateRequest),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @DisplayName("프로필 생성 - 특수문자가 포함된 이름")
    void createProfile_WithSpecialCharactersInName_Success() throws Exception {
        // Given - 하이픈이 포함된 이름 (외국인 이름)
        var request = createProfileRequest(
            "Jean-Pierre",
            "O'Brien",
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then - 특수문자가 포함된 이름도 허용
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isCreated());
    }
    
    @Test
    @DisplayName("프로필 생성 - 매우 긴 이름")
    void createProfile_WithVeryLongName_Failure() throws Exception {
        // Given - 100자 이상의 긴 이름
        String longName = "A".repeat(101);
        
        var request = createProfileRequest(
            longName,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
        
        // When & Then
        mockMvc.perform(authenticatedJsonRequest(
                post(PROFILES_API_PATH),
                objectMapper.writeValueAsString(request),
                authenticatedAccount.getAccessToken()))
                .andExpect(status().isBadRequest());
    }
}