package com.commerce.customer.core.domain.service.profile;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.*;
import com.commerce.customer.core.domain.repository.profile.CustomerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerProfileDomainService 테스트")
class CustomerProfileDomainServiceTest {

    @Mock
    private CustomerProfileRepository profileRepository;

    @Mock
    private AddressValidationService addressValidationService;

    private CustomerProfileDomainService domainService;

    @BeforeEach
    void setUp() {
        domainService = new CustomerProfileDomainService(profileRepository, addressValidationService);
    }

    @Test
    @DisplayName("유효한 정보로 프로필을 생성할 수 있다")
    void createProfile() {
        // Given
        CustomerId customerId = CustomerId.generate();
        PersonalInfo personalInfo = createValidPersonalInfo();
        ContactInfo contactInfo = createValidContactInfo();
        
        given(profileRepository.existsByCustomerId(customerId)).willReturn(false);
        given(profileRepository.save(any(CustomerProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerProfile result = domainService.createProfile(customerId, personalInfo, contactInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getPersonalInfo()).isEqualTo(personalInfo);
        assertThat(result.getContactInfo()).isEqualTo(contactInfo);
        verify(profileRepository).save(any(CustomerProfile.class));
    }

    @Test
    @DisplayName("이미 프로필이 존재하는 고객에 대해 프로필 생성 시 예외가 발생한다")
    void throwExceptionWhenProfileAlreadyExists() {
        // Given
        CustomerId customerId = CustomerId.generate();
        PersonalInfo personalInfo = createValidPersonalInfo();
        ContactInfo contactInfo = createValidContactInfo();
        
        given(profileRepository.existsByCustomerId(customerId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> domainService.createProfile(customerId, personalInfo, contactInfo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이미 프로필이 존재하는 고객입니다.");
    }

    @Test
    @DisplayName("개인정보를 업데이트할 수 있다")
    void updatePersonalInfo() {
        // Given
        ProfileId profileId = ProfileId.generate();
        CustomerProfile existingProfile = createValidProfile();
        PersonalInfo newPersonalInfo = PersonalInfo.of(
            FullName.of("영희", "김"),
            BirthDate.of(LocalDate.of(1995, 3, 20)),
            Gender.FEMALE
        );
        
        given(profileRepository.findById(profileId)).willReturn(Optional.of(existingProfile));
        given(profileRepository.save(any(CustomerProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerProfile result = domainService.updatePersonalInfo(profileId, newPersonalInfo);

        // Then
        assertThat(result.getPersonalInfo()).isEqualTo(newPersonalInfo);
        verify(profileRepository).save(existingProfile);
    }

    @Test
    @DisplayName("존재하지 않는 프로필의 개인정보 업데이트 시 예외가 발생한다")
    void throwExceptionWhenUpdateNonExistentProfile() {
        // Given
        ProfileId profileId = ProfileId.generate();
        PersonalInfo personalInfo = createValidPersonalInfo();
        
        given(profileRepository.findById(profileId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> domainService.updatePersonalInfo(profileId, personalInfo))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 프로필입니다.");
    }

    @Test
    @DisplayName("주소를 검증하고 추가할 수 있다")
    void validateAndAddAddress() {
        // Given
        ProfileId profileId = ProfileId.generate();
        CustomerProfile existingProfile = createValidProfile();
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");
        
        given(profileRepository.findById(profileId)).willReturn(Optional.of(existingProfile));
        given(addressValidationService.validateAddress("12345", "서울특별시 강남구 테헤란로 123")).willReturn(true);
        given(profileRepository.save(any(CustomerProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerProfile result = domainService.validateAndAddAddress(profileId, address);

        // Then
        assertThat(result.getAddresses()).hasSize(1);
        assertThat(result.getAddresses().get(0).getZipCode()).isEqualTo("12345");
        verify(addressValidationService).validateAddress("12345", "서울특별시 강남구 테헤란로 123");
        verify(profileRepository).save(existingProfile);
    }

    @Test
    @DisplayName("유효하지 않은 주소 추가 시 예외가 발생한다")
    void throwExceptionWhenAddInvalidAddress() {
        // Given
        ProfileId profileId = ProfileId.generate();
        CustomerProfile existingProfile = createValidProfile();
        Address address = Address.create(AddressType.HOME, "집", "12345", "잘못된주소", null, "456호");
        
        given(profileRepository.findById(profileId)).willReturn(Optional.of(existingProfile));
        given(addressValidationService.validateAddress("12345", "잘못된주소")).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> domainService.validateAndAddAddress(profileId, address))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("유효하지 않은 주소입니다.");
    }

    @Test
    @DisplayName("기본 주소를 설정할 수 있다")
    void setDefaultAddress() {
        // Given
        ProfileId profileId = ProfileId.generate();
        CustomerProfile existingProfile = createValidProfile();
        Address address1 = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");
        Address address2 = Address.create(AddressType.WORK, "회사", "54321", "부산광역시 해운대구 센텀로 456", null, "789호");
        existingProfile.addAddress(address1);
        existingProfile.addAddress(address2);
        
        given(profileRepository.findById(profileId)).willReturn(Optional.of(existingProfile));
        given(profileRepository.save(any(CustomerProfile.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        CustomerProfile result = domainService.setDefaultAddress(profileId, address2.getAddressId());

        // Then
        assertThat(result.getAddresses().stream()
            .filter(Address::isDefault)
            .findFirst()
            .orElseThrow().getAddressId()).isEqualTo(address2.getAddressId());
        verify(profileRepository).save(existingProfile);
    }

    @Test
    @DisplayName("한국 전화번호 형식을 검증할 수 있다")
    void validatePhoneNumber() {
        // Given
        PhoneNumber validPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber invalidPhone = PhoneNumber.of("+82", "010-123-4567");

        // When & Then
        assertThatCode(() -> domainService.validatePhoneNumber(validPhone))
            .doesNotThrowAnyException();
        
        assertThatThrownBy(() -> domainService.validatePhoneNumber(invalidPhone))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("올바르지 않은 한국 전화번호 형식입니다.");
    }

    @Test
    @DisplayName("주소 형식을 검증할 수 있다")
    void validateAddress() {
        // Given
        Address validAddress = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");

        given(addressValidationService.validateAddress("12345", "서울특별시 강남구 테헤란로 123")).willReturn(true);

        // When & Then
        assertThatCode(() -> domainService.validateAddress(validAddress))
            .doesNotThrowAnyException();
        
        // 잘못된 주소는 Address 생성 시점에서 이미 검증됨
        given(addressValidationService.validateAddress("12345", "서울특별시 강남구 테헤란로 123")).willReturn(false);
        
        assertThatThrownBy(() -> domainService.validateAddress(validAddress))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("유효하지 않은 주소입니다.");
    }

    private CustomerProfile createValidProfile() {
        return CustomerProfile.create(
            CustomerId.generate(),
            createValidPersonalInfo(),
            createValidContactInfo()
        );
    }

    private PersonalInfo createValidPersonalInfo() {
        return PersonalInfo.of(
            FullName.of("길동", "홍"),
            BirthDate.of(LocalDate.of(1990, 5, 15)),
            Gender.MALE
        );
    }

    private ContactInfo createValidContactInfo() {
        return ContactInfo.of(PhoneNumber.ofKorean("010-1234-5678"));
    }
}