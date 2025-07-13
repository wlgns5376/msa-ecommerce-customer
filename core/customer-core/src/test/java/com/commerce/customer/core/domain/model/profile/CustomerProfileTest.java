package com.commerce.customer.core.domain.model.profile;

import com.commerce.customer.core.domain.model.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CustomerProfile 애그리게이트 루트 테스트")
class CustomerProfileTest {

    @Test
    @DisplayName("유효한 정보로 고객 프로필을 생성할 수 있다")
    void createCustomerProfileWithValidInfo() {
        // Given
        CustomerId customerId = CustomerId.generate();
        PersonalInfo personalInfo = createValidPersonalInfo();
        ContactInfo contactInfo = createValidContactInfo();

        // When
        CustomerProfile profile = CustomerProfile.create(customerId, personalInfo, contactInfo);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getProfileId()).isNotNull();
        assertThat(profile.getCustomerId()).isEqualTo(customerId);
        assertThat(profile.getPersonalInfo()).isEqualTo(personalInfo);
        assertThat(profile.getContactInfo()).isEqualTo(contactInfo);
        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(profile.getAddresses()).isEmpty();
        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("개인정보를 업데이트할 수 있다")
    void updatePersonalInfo() {
        // Given
        CustomerProfile profile = createValidProfile();
        PersonalInfo newPersonalInfo = PersonalInfo.of(
            FullName.of("영희", "김"),
            BirthDate.of(LocalDate.of(1995, 3, 20)),
            Gender.FEMALE
        );

        // When
        profile.updatePersonalInfo(newPersonalInfo);

        // Then
        assertThat(profile.getPersonalInfo()).isEqualTo(newPersonalInfo);
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("연락처 정보를 업데이트할 수 있다")
    void updateContactInfo() {
        // Given
        CustomerProfile profile = createValidProfile();
        ContactInfo newContactInfo = ContactInfo.of(PhoneNumber.ofKorean("010-9999-9999"));

        // When
        profile.updateContactInfo(newContactInfo);

        // Then
        assertThat(profile.getContactInfo()).isEqualTo(newContactInfo);
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("주소를 추가할 수 있다")
    void addAddress() {
        // Given
        CustomerProfile profile = createValidProfile();
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");

        // When
        profile.addAddress(address);

        // Then
        assertThat(profile.getAddresses()).hasSize(1);
        assertThat(profile.getAddresses().get(0)).isEqualTo(address);
        assertThat(address.isDefault()).isTrue(); // 첫 번째 주소는 자동으로 기본 주소가 됨
    }

    @Test
    @DisplayName("주소를 삭제할 수 있다")
    void removeAddress() {
        // Given
        CustomerProfile profile = createValidProfile();
        Address address1 = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");
        Address address2 = Address.create(AddressType.WORK, "회사", "54321", "부산광역시 해운대구 센텀로 456", null, "789호");
        profile.addAddress(address1);
        profile.addAddress(address2);

        // When
        profile.removeAddress(address1.getAddressId());

        // Then
        assertThat(profile.getAddresses()).hasSize(1);
        assertThat(profile.getAddresses().get(0)).isEqualTo(address2);
        assertThat(address2.isDefault()).isTrue(); // 기본 주소가 자동으로 변경됨
    }

    @Test
    @DisplayName("기본 주소를 설정할 수 있다")
    void setDefaultAddress() {
        // Given
        CustomerProfile profile = createValidProfile();
        Address address1 = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");
        Address address2 = Address.create(AddressType.WORK, "회사", "54321", "부산광역시 해운대구 센텀로 456", null, "789호");
        profile.addAddress(address1);
        profile.addAddress(address2);

        // When
        profile.setDefaultAddress(address2.getAddressId());

        // Then
        assertThat(address1.isDefault()).isFalse();
        assertThat(address2.isDefault()).isTrue();
    }

    @Test
    @DisplayName("선호도를 업데이트할 수 있다")
    void updatePreferences() {
        // Given
        CustomerProfile profile = createValidProfile();
        ProfilePreferences preferences = ProfilePreferences.builder()
            .categoryInterests(List.of(
                CategoryInterest.of("FASHION", "패션", InterestLevel.HIGH),
                CategoryInterest.of("ELECTRONICS", "전자제품", InterestLevel.MEDIUM)
            ))
            .notificationSettings(NotificationSettings.builder()
                .emailNotification(true)
                .smsNotification(false)
                .pushNotification(true)
                .orderUpdates(true)
                .promotionalOffers(false)
                .build())
            .marketingConsent(MarketingConsent.builder()
                .emailMarketing(true)
                .smsMarketing(false)
                .personalizedAds(true)
                .build())
            .build();

        // When
        profile.updatePreferences(preferences);

        // Then
        assertThat(profile.getPreferences()).isEqualTo(preferences);
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("프로필을 비활성화할 수 있다")
    void deactivateProfile() {
        // Given
        CustomerProfile profile = createValidProfile();

        // When
        profile.deactivate();

        // Then
        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.INACTIVE);
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("프로필을 활성화할 수 있다")
    void activateProfile() {
        // Given
        CustomerProfile profile = createValidProfile();
        profile.deactivate();

        // When
        profile.activate();

        // Then
        assertThat(profile.getStatus()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("최대 주소 개수를 초과하면 예외가 발생한다")
    void throwExceptionWhenExceedMaxAddresses() {
        // Given
        CustomerProfile profile = createValidProfile();
        
        // 10개의 주소 추가
        for (int i = 1; i <= 10; i++) {
            Address address = Address.create(AddressType.HOME, "주소" + i, "1234" + i % 10, 
                "서울특별시 강남구 테헤란로 " + i, null, i + "호");
            profile.addAddress(address);
        }

        // When & Then
        Address extraAddress = Address.create(AddressType.HOME, "추가주소", "12345", 
            "서울특별시 강남구 테헤란로 999", null, "999호");
        assertThatThrownBy(() -> profile.addAddress(extraAddress))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("주소는 최대 10개까지 등록 가능합니다.");
    }

    @Test
    @DisplayName("존재하지 않는 주소를 삭제하려 하면 예외가 발생한다")
    void throwExceptionWhenRemoveNonExistentAddress() {
        // Given
        CustomerProfile profile = createValidProfile();
        AddressId nonExistentAddressId = AddressId.generate();

        // When & Then
        assertThatThrownBy(() -> profile.removeAddress(nonExistentAddressId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("존재하지 않는 주소입니다.");
    }

    @Test
    @DisplayName("마지막 주소를 삭제하려 하면 예외가 발생한다")
    void throwExceptionWhenRemoveLastAddress() {
        // Given
        CustomerProfile profile = createValidProfile();
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", null, "456호");
        profile.addAddress(address);

        // When & Then
        assertThatThrownBy(() -> profile.removeAddress(address.getAddressId()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("최소 하나의 주소는 유지되어야 합니다.");
    }

    @Test
    @DisplayName("동일한 ID를 가진 CustomerProfile은 같다고 판단된다")
    void equalityTest() {
        // Given
        CustomerProfile profile1 = createValidProfile();
        CustomerProfile profile2 = createValidProfile();

        // When & Then
        assertThat(profile1).isEqualTo(profile1); // 자기 자신과는 같음
        assertThat(profile1).isNotEqualTo(profile2); // 다른 프로필과는 다름
        assertThat(profile1.hashCode()).isEqualTo(profile1.hashCode());
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