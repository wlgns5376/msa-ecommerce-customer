package com.commerce.customer.api.controller;

import com.commerce.customer.api.dto.profile.AddressRequest;
import com.commerce.customer.api.dto.profile.CreateProfileRequest;
import com.commerce.customer.api.dto.profile.CreateProfileResponse;
import com.commerce.customer.api.dto.profile.ProfileResponse;
import com.commerce.customer.api.dto.profile.UpdateProfileRequest;
import com.commerce.customer.api.dto.profile.UpdateProfileResponse;
import com.commerce.customer.api.exception.ResourceNotFoundException;
import com.commerce.customer.core.application.service.CustomerProfileApplicationService;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.jwt.JwtClaims;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import com.commerce.customer.core.domain.model.profile.Address;
import com.commerce.customer.core.domain.model.profile.AddressId;
import com.commerce.customer.core.domain.model.profile.AddressType;
import com.commerce.customer.core.domain.model.profile.BirthDate;
import com.commerce.customer.core.domain.model.profile.ContactInfo;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.FullName;
import com.commerce.customer.core.domain.model.profile.Gender;
import com.commerce.customer.core.domain.model.profile.MarketingConsent;
import com.commerce.customer.core.domain.model.profile.NotificationSettings;
import com.commerce.customer.core.domain.model.profile.PersonalInfo;
import com.commerce.customer.core.domain.model.profile.PhoneNumber;
import com.commerce.customer.core.domain.model.profile.ProfileId;
import com.commerce.customer.core.domain.model.profile.ProfilePreferences;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerProfileController 단위 테스트")
class CustomerProfileControllerTest {

    @Mock
    private CustomerProfileApplicationService customerProfileApplicationService;

    @InjectMocks
    private CustomerProfileController customerProfileController;

    private HttpServletRequest httpRequest;
    private JwtClaims validJwtClaims;
    private AccountId testAccountId;
    private ProfileId testProfileId;
    private CustomerId testCustomerId;

    @BeforeEach
    void setUp() {
        httpRequest = mock(HttpServletRequest.class);
        testAccountId = AccountId.of(1L);
        testProfileId = ProfileId.of(1L);
        testCustomerId = CustomerId.of(1L);
        
        validJwtClaims = JwtClaims.create(
            testCustomerId,
            testAccountId,
            Email.of("test@example.com"),
            "issuer",
            "audience",
            JwtTokenType.ACCESS
        );
    }

    @Nested
    @DisplayName("프로필 생성 테스트")
    class CreateProfileTest {

        @Test
        @DisplayName("성공: 모든 정보가 포함된 프로필 생성")
        void createProfile_WithFullInfo_Success() {
            // given
            CreateProfileRequest request = new CreateProfileRequest(
                "홍", "길동", LocalDate.of(1990, 1, 1), "MALE", "010-1234-5678"
            );
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);
            given(customerProfileApplicationService.createProfile(any(), any(), any()))
                .willReturn(testProfileId);

            // when
            ResponseEntity<CreateProfileResponse> response = 
                customerProfileController.createProfile(request, httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getProfileId()).isEqualTo(1L);
            assertThat(response.getBody().getMessage()).isEqualTo("프로필이 성공적으로 생성되었습니다.");
            
            then(customerProfileApplicationService).should()
                .createProfile(eq(testAccountId), any(PersonalInfo.class), any(ContactInfo.class));
        }

        @Test
        @DisplayName("성공: 최소 정보만 포함된 프로필 생성")
        void createProfile_WithMinimalInfo_Success() {
            // given
            CreateProfileRequest request = new CreateProfileRequest(
                "홍", "길동", null, null, "010-1234-5678"
            );
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);
            given(customerProfileApplicationService.createProfile(any(), any(), any()))
                .willReturn(testProfileId);

            // when
            ResponseEntity<CreateProfileResponse> response = 
                customerProfileController.createProfile(request, httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getProfileId()).isEqualTo(1L);
            
            then(customerProfileApplicationService).should()
                .createProfile(eq(testAccountId), any(PersonalInfo.class), any(ContactInfo.class));
        }

        @Test
        @DisplayName("실패: JWT 토큰이 없는 경우")
        void createProfile_WithoutJwt_ShouldReturnUnauthorized() {
            // given
            CreateProfileRequest request = new CreateProfileRequest(
                "홍", "길동", LocalDate.of(1990, 1, 1), "MALE", "010-1234-5678"
            );
            given(httpRequest.getAttribute("jwtClaims")).willReturn(null);

            // when
            ResponseEntity<CreateProfileResponse> response = 
                customerProfileController.createProfile(request, httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNull();
            
            then(customerProfileApplicationService).should(never())
                .createProfile(any(), any(), any());
        }

        @Test
        @DisplayName("실패: 전화번호가 null인 경우 NullPointerException 발생")
        void createProfile_WithNullPhoneNumber_ShouldThrowException() {
            // given
            CreateProfileRequest request = new CreateProfileRequest(
                "홍", "길동", LocalDate.of(1990, 1, 1), "MALE", null
            );
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);

            // when & then
            assertThatThrownBy(() -> customerProfileController.createProfile(request, httpRequest))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("주 연락처는 필수값입니다");
            
            then(customerProfileApplicationService).should(never())
                .createProfile(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("프로필 조회 테스트")
    class GetProfileTest {

        @Test
        @DisplayName("성공: 내 프로필 조회")
        void getMyProfile_Success() {
            // given
            CustomerProfile mockProfile = createMockProfile();
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);
            given(customerProfileApplicationService.getProfileByAccountId(testAccountId))
                .willReturn(mockProfile);

            // when
            ResponseEntity<ProfileResponse> response = 
                customerProfileController.getMyProfile(httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            then(customerProfileApplicationService).should()
                .getProfileByAccountId(testAccountId);
        }

        @Test
        @DisplayName("실패: JWT 토큰이 없는 경우")
        void getMyProfile_WithoutJwt_ShouldReturnUnauthorized() {
            // given
            given(httpRequest.getAttribute("jwtClaims")).willReturn(null);

            // when
            ResponseEntity<ProfileResponse> response = 
                customerProfileController.getMyProfile(httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNull();
            
            then(customerProfileApplicationService).should(never())
                .getProfileByAccountId(any());
        }

        @Test
        @DisplayName("실패: 프로필을 찾을 수 없는 경우")
        void getMyProfile_ProfileNotFound_ShouldThrowException() {
            // given
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);
            given(customerProfileApplicationService.getProfileByAccountId(testAccountId))
                .willThrow(new IllegalArgumentException("프로필을 찾을 수 없습니다"));

            // when & then
            assertThatThrownBy(() -> customerProfileController.getMyProfile(httpRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("프로필을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: ID로 프로필 조회")
        void getProfile_ById_Success() {
            // given
            CustomerProfile mockProfile = createMockProfile();
            given(customerProfileApplicationService.getProfile(testProfileId))
                .willReturn(mockProfile);

            // when
            ResponseEntity<CustomerProfile> response = 
                customerProfileController.getProfile(1L);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(mockProfile);
            
            then(customerProfileApplicationService).should()
                .getProfile(testProfileId);
        }
    }

    @Nested
    @DisplayName("프로필 수정 테스트")
    class UpdateProfileTest {

        @Test
        @DisplayName("성공: 전화번호 수정")
        void updateProfile_PhoneNumber_Success() {
            // given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                .phoneNumber("010-9876-5432")
                .build();
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);

            // when
            ResponseEntity<UpdateProfileResponse> response = 
                customerProfileController.updateProfile(request, httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            
            then(customerProfileApplicationService).should()
                .updatePhoneNumber(eq(testAccountId), any(PhoneNumber.class));
        }

        @Test
        @DisplayName("성공: 전화번호가 null인 경우 업데이트하지 않음")
        void updateProfile_NullPhoneNumber_ShouldNotUpdate() {
            // given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                .phoneNumber(null)
                .build();
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);

            // when
            ResponseEntity<UpdateProfileResponse> response = 
                customerProfileController.updateProfile(request, httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            then(customerProfileApplicationService).should(never())
                .updatePhoneNumber(any(), any());
        }

        @Test
        @DisplayName("실패: JWT 토큰이 없는 경우")
        void updateProfile_WithoutJwt_ShouldReturnUnauthorized() {
            // given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                .phoneNumber("010-9876-5432")
                .build();
            given(httpRequest.getAttribute("jwtClaims")).willReturn(null);

            // when
            ResponseEntity<UpdateProfileResponse> response = 
                customerProfileController.updateProfile(request, httpRequest);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isNull();
            
            then(customerProfileApplicationService).should(never())
                .updatePhoneNumber(any(), any());
        }

        @Test
        @DisplayName("실패: 프로필을 찾을 수 없는 경우")
        void updateProfile_ProfileNotFound_ShouldThrowException() {
            // given
            UpdateProfileRequest request = UpdateProfileRequest.builder()
                .phoneNumber("010-9876-5432")
                .build();
            given(httpRequest.getAttribute("jwtClaims")).willReturn(validJwtClaims);
            doThrow(new IllegalArgumentException("프로필을 찾을 수 없습니다"))
                .when(customerProfileApplicationService).updatePhoneNumber(any(), any());

            // when & then
            assertThatThrownBy(() -> customerProfileController.updateProfile(request, httpRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("프로필을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("주소 관리 테스트")
    class AddressManagementTest {

        @Test
        @DisplayName("성공: 주소 추가")
        void addAddress_Success() {
            // given
            AddressRequest request = new AddressRequest(
                "HOME", "서울시 강남구 테헤란로 123", "서울시 강남구 대치동 123-45",
                "타워빌딩 10층", "12345", true
            );

            // when
            ResponseEntity<Void> response = 
                customerProfileController.addAddress(1L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            
            then(customerProfileApplicationService).should()
                .addAddress(eq(testProfileId), any(Address.class));
        }

        @Test
        @DisplayName("성공: 기본 주소로 설정하지 않는 경우")
        void addAddress_NotDefault_Success() {
            // given
            AddressRequest request = new AddressRequest(
                "WORK", "서울시 강남구 테헤란로 456", "서울시 강남구 대치동 456-78",
                "오피스빌딩 5층", "54321", false
            );

            // when
            ResponseEntity<Void> response = 
                customerProfileController.addAddress(1L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            
            then(customerProfileApplicationService).should()
                .addAddress(eq(testProfileId), any(Address.class));
        }

        @Test
        @DisplayName("성공: 주소 수정")
        void updateAddress_Success() {
            // given
            AddressRequest request = new AddressRequest(
                "HOME", "부산시 해운대구 센텀로 789", "부산시 해운대구 우동 789-12",
                "센텀타워 20층", "48058", true
            );

            // when
            ResponseEntity<Void> response = 
                customerProfileController.updateAddress(1L, 1L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            
            then(customerProfileApplicationService).should()
                .updateAddress(eq(testProfileId), any(Address.class));
        }
    }

    private CustomerProfile createMockProfile() {
        PersonalInfo personalInfo = PersonalInfo.of(
            FullName.of("홍", "길동"),
            BirthDate.of(LocalDate.of(1990, 1, 1)),
            Gender.MALE
        );
        
        ContactInfo contactInfo = ContactInfo.of(
            PhoneNumber.ofKorean("010-1234-5678")
        );
        
        return CustomerProfile.create(
            testCustomerId,
            personalInfo,
            contactInfo
        );
    }
}