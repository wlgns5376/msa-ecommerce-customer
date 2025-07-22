package com.commerce.customer.api.integration.fixture;

import com.commerce.customer.api.dto.profile.CreateProfileRequest;
import com.commerce.customer.api.dto.profile.UpdateProfileRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 프로필 관련 테스트 Fixture Factory
 */
public class ProfileTestFixture {
    
    /**
     * 기본 프로필 생성 요청 DTO 생성
     */
    public static CreateProfileRequest createProfileRequest() {
        return new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            TEST_BIRTH_DATE,
            TEST_GENDER,
            TEST_PHONE
        );
    }
    
    /**
     * 커스텀 프로필 생성 요청 DTO 생성
     */
    public static CreateProfileRequest createProfileRequest(String firstName, String lastName, 
                                                          LocalDate birthDate, String gender, String phone) {
        return new CreateProfileRequest(firstName, lastName, birthDate, gender, phone);
    }
    
    /**
     * 최소 정보만 포함된 프로필 생성 요청 DTO 생성
     */
    public static CreateProfileRequest createMinimalProfileRequest() {
        return new CreateProfileRequest(
            TEST_FIRST_NAME,
            TEST_LAST_NAME,
            null,  // 생년월일 선택적
            null,  // 성별 선택적
            TEST_PHONE
        );
    }
    
    /**
     * 프로필 수정 요청 DTO 생성 - 전화번호만 수정
     */
    public static UpdateProfileRequest updatePhoneNumberRequest(String newPhoneNumber) {
        return UpdateProfileRequest.builder()
                .phoneNumber(newPhoneNumber)
                .build();
    }
    
    /**
     * 프로필 수정 요청 DTO 생성 - 전체 정보 수정
     */
    public static UpdateProfileRequest updateFullProfileRequest() {
        return UpdateProfileRequest.builder()
                .phoneNumber(TEST_PHONE_UPDATED)
                .address(UpdateProfileRequest.AddressRequest.builder()
                        .streetAddress(TEST_ROAD_ADDRESS)
                        .detailAddress(TEST_DETAIL_ADDRESS)
                        .postalCode(TEST_ZIPCODE)
                        .jibunAddress(TEST_JIBUN_ADDRESS)
                        .build())
                .notificationSettings(UpdateProfileRequest.NotificationSettingsRequest.builder()
                        .emailNotification(true)
                        .smsNotification(true)
                        .pushNotification(false)
                        .build())
                .marketingConsent(UpdateProfileRequest.MarketingConsentRequest.builder()
                        .emailMarketing(true)
                        .smsMarketing(false)
                        .build())
                .build();
    }
    
    /**
     * 프로필 생성 및 ID 반환
     */
    public static Long createProfile(MockMvc mockMvc, ObjectMapper objectMapper, String accessToken) throws Exception {
        return createProfile(mockMvc, objectMapper, accessToken, createProfileRequest());
    }
    
    /**
     * 커스텀 데이터로 프로필 생성 및 ID 반환
     */
    public static Long createProfile(MockMvc mockMvc, ObjectMapper objectMapper, String accessToken,
                                   CreateProfileRequest request) throws Exception {
        MvcResult result = mockMvc.perform(post(PROFILES_API_PATH)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("profileId").asLong();
    }
    
    /**
     * 프로필 수정
     */
    public static void updateProfile(MockMvc mockMvc, ObjectMapper objectMapper, String accessToken,
                                   UpdateProfileRequest request) throws Exception {
        mockMvc.perform(post(PROFILES_API_PATH)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    /**
     * 프로필 테스트 데이터 클래스
     */
    public static class ProfileTestData {
        private final Long profileId;
        private final String firstName;
        private final String lastName;
        private final LocalDate birthDate;
        private final String gender;
        private final String phoneNumber;
        
        public ProfileTestData(Long profileId, String firstName, String lastName,
                             LocalDate birthDate, String gender, String phoneNumber) {
            this.profileId = profileId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthDate = birthDate;
            this.gender = gender;
            this.phoneNumber = phoneNumber;
        }
        
        public Long getProfileId() { return profileId; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public LocalDate getBirthDate() { return birthDate; }
        public String getGender() { return gender; }
        public String getPhoneNumber() { return phoneNumber; }
    }
}