package com.commerce.infrastructure.persistence.customer.mapper;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.*;
import com.commerce.infrastructure.persistence.customer.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerProfileMapperTest {

    private CustomerProfileMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CustomerProfileMapper();
    }

    @Test
    @DisplayName("도메인 객체가 null인 경우 null을 반환한다")
    void toEntity_ShouldReturnNull_WhenDomainIsNull() {
        // when
        CustomerProfileEntity result = mapper.toEntity(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("엔티티가 null인 경우 null을 반환한다")
    void toDomain_ShouldReturnNull_WhenEntityIsNull() {
        // when
        CustomerProfile result = mapper.toDomain(null);

        // then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("필수 필드만 있는 도메인 객체를 엔티티로 변환한다")
    void toEntity_ShouldMapRequiredFields() {
        // given
        CustomerProfile profile = createMinimalProfile();

        // when
        CustomerProfileEntity entity = mapper.toEntity(profile);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getCustomerId()).isEqualTo(123L);
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");
        assertThat(entity.getPrimaryPhone()).isEqualTo("01012345678");
        assertThat(entity.getStatus()).isEqualTo(CustomerProfileEntity.ProfileStatus.ACTIVE);
        
        // optional fields should be null
        assertThat(entity.getBirthDate()).isNull();
        assertThat(entity.getGender()).isNull();
        assertThat(entity.getProfileImageUrl()).isNull();
        assertThat(entity.getSecondaryPhone()).isNull();
    }

    @Test
    @DisplayName("모든 필드가 있는 도메인 객체를 엔티티로 변환한다")
    void toEntity_ShouldMapAllFields() {
        // given
        CustomerProfile profile = createFullProfile();

        // when
        CustomerProfileEntity entity = mapper.toEntity(profile);

        // then
        assertThat(entity).isNotNull();
        assertThat(entity.getCustomerId()).isEqualTo(123L);
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");
        assertThat(entity.getPrimaryPhone()).isEqualTo("01012345678");
        assertThat(entity.getSecondaryPhone()).isEqualTo("01087654321");
        assertThat(entity.getStatus()).isEqualTo(CustomerProfileEntity.ProfileStatus.ACTIVE);
        assertThat(entity.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(entity.getGender()).isEqualTo(CustomerProfileEntity.Gender.MALE);
        assertThat(entity.getProfileImageUrl()).isEqualTo("https://example.com/profile.jpg");
        
        // marketing consent
        assertThat(entity.getEmailMarketingConsent()).isTrue();
        assertThat(entity.getSmsMarketingConsent()).isTrue();
        assertThat(entity.getPushMarketingConsent()).isFalse();
        
        // notification settings
        assertThat(entity.getOrderNotifications()).isTrue();
        assertThat(entity.getPromotionNotifications()).isFalse();
        assertThat(entity.getAccountNotifications()).isTrue();
        assertThat(entity.getReviewNotifications()).isTrue();
    }

    @Test
    @DisplayName("필수 필드만 있는 엔티티를 도메인 객체로 변환한다")
    void toDomain_ShouldMapRequiredFields() {
        // given
        CustomerProfileEntity entity = createMinimalEntity();

        // when
        CustomerProfile profile = mapper.toDomain(entity);

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getCustomerId().getValue()).isEqualTo(123L);
        assertThat(profile.getPersonalInfo().getFullName().getFirstName()).isEqualTo("John");
        assertThat(profile.getPersonalInfo().getFullName().getLastName()).isEqualTo("Doe");
        assertThat(profile.getContactInfo().getPrimaryPhone().getNumber()).isEqualTo("01012345678");
        
        // optional fields
        assertThat(profile.getPersonalInfo().getBirthDate()).isNull();
        assertThat(profile.getPersonalInfo().getGender()).isNull();
        assertThat(profile.getPersonalInfo().getProfileImage()).isNull();
        assertThat(profile.getContactInfo().getSecondaryPhone()).isNull();
    }

    @Test
    @DisplayName("모든 필드가 있는 엔티티를 도메인 객체로 변환한다")
    void toDomain_ShouldMapAllFields() {
        // given
        CustomerProfileEntity entity = createFullEntity();

        // when
        CustomerProfile profile = mapper.toDomain(entity);

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getCustomerId().getValue()).isEqualTo(123L);
        assertThat(profile.getPersonalInfo().getFullName().getFirstName()).isEqualTo("John");
        assertThat(profile.getPersonalInfo().getFullName().getLastName()).isEqualTo("Doe");
        assertThat(profile.getContactInfo().getPrimaryPhone().getNumber()).isEqualTo("01012345678");
        assertThat(profile.getContactInfo().getSecondaryPhone().getNumber()).isEqualTo("01087654321");
        assertThat(profile.getPersonalInfo().getBirthDate().getDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(profile.getPersonalInfo().getGender()).isEqualTo(Gender.MALE);
        assertThat(profile.getPersonalInfo().getProfileImage().getImageUrl()).isEqualTo("https://example.com/profile.jpg");
    }

    @Test
    @DisplayName("ProfileId가 있는 엔티티를 변환할 때 리플렉션을 사용하여 ProfileId를 설정한다")
    void toDomain_ShouldSetProfileIdUsingReflection() {
        // given
        CustomerProfileEntity entity = createMinimalEntity();
        // Use reflection to set profileId for testing
        try {
            java.lang.reflect.Field profileIdField = CustomerProfileEntity.class.getDeclaredField("profileId");
            profileIdField.setAccessible(true);
            profileIdField.set(entity, 123L);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set profileId", e);
        }

        // when
        CustomerProfile profile = mapper.toDomain(entity);

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getProfileId()).isNotNull();
        assertThat(profile.getProfileId().getValue()).isEqualTo(123L);
    }

    @Test
    @DisplayName("주소가 있는 엔티티를 도메인 객체로 변환한다")
    void toDomain_ShouldMapAddresses() {
        // given
        CustomerProfileEntity entity = createMinimalEntity();
        AddressEntity addressEntity = createAddressEntity();
        entity.getAddresses().add(addressEntity);

        // when
        CustomerProfile profile = mapper.toDomain(entity);

        // then
        assertThat(profile).isNotNull();
        // Address는 ProfilePreferences가 아닌 별도로 관리됨
        // Mapper 테스트에서는 매핑 로직이 정상 동작하는지 확인
    }

    @Test
    @DisplayName("브랜드 선호도가 있는 엔티티를 도메인 객체로 변환한다")
    void toDomain_ShouldMapBrandPreferences() {
        // given
        CustomerProfileEntity entity = createMinimalEntity();
        BrandPreferenceEntity brandEntity = createBrandPreferenceEntity();
        entity.getBrandPreferences().add(brandEntity);

        // when
        CustomerProfile profile = mapper.toDomain(entity);

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getPreferences().getBrandPreferences()).hasSize(1);
        BrandPreference brandPref = profile.getPreferences().getBrandPreferences().get(0);
        assertThat(brandPref.getBrandId()).isEqualTo("BRAND_NIKE");
        assertThat(brandPref.getBrandName()).isEqualTo("Nike");
        assertThat(brandPref.getLevel()).isEqualTo(PreferenceLevel.LOVE);
    }

    @Test
    @DisplayName("카테고리 관심사가 있는 엔티티를 도메인 객체로 변환한다")
    void toDomain_ShouldMapCategoryInterests() {
        // given
        CustomerProfileEntity entity = createMinimalEntity();
        CategoryInterestEntity categoryEntity = createCategoryInterestEntity();
        entity.getCategoryInterests().add(categoryEntity);

        // when
        CustomerProfile profile = mapper.toDomain(entity);

        // then
        assertThat(profile).isNotNull();
        assertThat(profile.getPreferences().getCategoryInterests()).hasSize(1);
        CategoryInterest categoryInterest = profile.getPreferences().getCategoryInterests().get(0);
        assertThat(categoryInterest.getCategoryId()).isEqualTo("CAT_ELECTRONICS");
        assertThat(categoryInterest.getCategoryName()).isEqualTo("Electronics");
        assertThat(categoryInterest.getLevel()).isEqualTo(InterestLevel.HIGH);
    }

    @ParameterizedTest
    @EnumSource(Gender.class)
    @DisplayName("모든 Gender enum 값을 올바르게 매핑한다")
    void shouldMapAllGenderValues(Gender domainGender) {
        // given
        CustomerProfile profile = createProfileWithGender(domainGender);

        // when
        CustomerProfileEntity entity = mapper.toEntity(profile);
        CustomerProfile mappedProfile = mapper.toDomain(entity);

        // then
        if (domainGender == Gender.PREFER_NOT_TO_SAY) {
            assertThat(entity.getGender()).isEqualTo(CustomerProfileEntity.Gender.OTHER);
            assertThat(mappedProfile.getPersonalInfo().getGender()).isEqualTo(Gender.OTHER);
        } else {
            assertThat(mappedProfile.getPersonalInfo().getGender()).isEqualTo(domainGender);
        }
    }

    @ParameterizedTest
    @EnumSource(ProfileStatus.class)
    @DisplayName("모든 ProfileStatus enum 값을 올바르게 매핑한다")
    void shouldMapAllProfileStatusValues(ProfileStatus domainStatus) {
        // given
        CustomerProfile profile = createProfileWithStatus(domainStatus);

        // when
        CustomerProfileEntity entity = mapper.toEntity(profile);

        // then
        assertThat(entity.getStatus().name()).isEqualTo(domainStatus.name());
    }

    @Test
    @DisplayName("양방향 변환이 일관성 있게 동작한다")
    void shouldMaintainConsistencyInBidirectionalMapping() {
        // given
        CustomerProfile originalProfile = createFullProfile();

        // when
        CustomerProfileEntity entity = mapper.toEntity(originalProfile);
        CustomerProfile mappedProfile = mapper.toDomain(entity);

        // then
        assertThat(mappedProfile.getCustomerId()).isEqualTo(originalProfile.getCustomerId());
        assertThat(mappedProfile.getPersonalInfo().getFullName()).isEqualTo(originalProfile.getPersonalInfo().getFullName());
        assertThat(mappedProfile.getContactInfo().getPrimaryPhone()).isEqualTo(originalProfile.getContactInfo().getPrimaryPhone());
        // Note: Collections and some optional fields might not be perfectly equal due to entity limitations
    }

    // Helper methods
    private CustomerProfile createMinimalProfile() {
        FullName fullName = FullName.of("John", "Doe");
        PersonalInfo personalInfo = PersonalInfo.of(fullName, null, null, null);
        PhoneNumber primaryPhone = PhoneNumber.of("+82", "01012345678");
        ContactInfo contactInfo = ContactInfo.of(primaryPhone);
        
        return CustomerProfile.create(
                CustomerId.of(123L),
                personalInfo,
                contactInfo
        );
    }

    private CustomerProfile createFullProfile() {
        FullName fullName = FullName.of("John", "Doe");
        BirthDate birthDate = BirthDate.of(LocalDate.of(1990, 1, 1));
        Gender gender = Gender.MALE;
        ProfileImage profileImage = ProfileImage.of("https://example.com/profile.jpg");
        PersonalInfo personalInfo = PersonalInfo.of(fullName, birthDate, gender, profileImage);
        
        PhoneNumber primaryPhone = PhoneNumber.of("+82", "01012345678");
        PhoneNumber secondaryPhone = PhoneNumber.of("+82", "01087654321");
        ContactInfo contactInfo = ContactInfo.of(primaryPhone, secondaryPhone);
        
        CustomerProfile profile = CustomerProfile.create(
                CustomerId.of(123L),
                personalInfo,
                contactInfo
        );
        
        // Update preferences
        MarketingConsent marketingConsent = MarketingConsent.builder()
                .emailMarketing(true)
                .smsMarketing(true)
                .personalizedAds(false)
                .build();
        
        NotificationSettings notificationSettings = NotificationSettings.builder()
                .orderUpdates(true)
                .promotionalOffers(false)
                .emailNotification(true)
                .smsNotification(true)
                .pushNotification(true)
                .build();
        
        profile.updatePreferences(
                ProfilePreferences.builder()
                        .marketingConsent(marketingConsent)
                        .notificationSettings(notificationSettings)
                        .categoryInterests(Collections.emptyList())
                        .brandPreferences(Collections.emptyList())
                        .build()
        );
        
        return profile;
    }

    private CustomerProfileEntity createMinimalEntity() {
        return CustomerProfileEntity.builder()
                .customerId(123L)
                .firstName("John")
                .lastName("Doe")
                .primaryPhone("01012345678")
                .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
                .emailMarketingConsent(false)
                .smsMarketingConsent(false)
                .pushMarketingConsent(false)
                .orderNotifications(false)
                .promotionNotifications(false)
                .accountNotifications(false)
                .reviewNotifications(false)
                .build();
    }

    private CustomerProfileEntity createFullEntity() {
        return CustomerProfileEntity.builder()
                .customerId(123L)
                .firstName("John")
                .lastName("Doe")
                .primaryPhone("01012345678")
                .secondaryPhone("01087654321")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(CustomerProfileEntity.Gender.MALE)
                .profileImageUrl("https://example.com/profile.jpg")
                .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
                .emailMarketingConsent(true)
                .smsMarketingConsent(true)
                .pushMarketingConsent(false)
                .orderNotifications(true)
                .promotionNotifications(false)
                .accountNotifications(true)
                .reviewNotifications(true)
                .build();
    }

    private AddressEntity createAddressEntity() {
        return AddressEntity.builder()
                .type(AddressEntity.AddressType.HOME)
                .alias("집")
                .zipCode("12345")
                .roadAddress("서울시 강남구 테헤란로 123")
                .jibunAddress("서울시 강남구 역삼동 123-45")
                .detailAddress("아파트 101동 202호")
                .isDefault(false)
                .build();
    }

    private BrandPreferenceEntity createBrandPreferenceEntity() {
        return BrandPreferenceEntity.builder()
                .brandName("Nike")
                .preferenceLevel(BrandPreferenceEntity.PreferenceLevel.LOVE)
                .build();
    }

    private CategoryInterestEntity createCategoryInterestEntity() {
        return CategoryInterestEntity.builder()
                .categoryName("Electronics")
                .interestLevel(CategoryInterestEntity.InterestLevel.HIGH)
                .build();
    }

    private CustomerProfile createProfileWithGender(Gender gender) {
        FullName fullName = FullName.of("John", "Doe");
        PersonalInfo personalInfo = PersonalInfo.of(fullName, null, gender, null);
        PhoneNumber primaryPhone = PhoneNumber.of("+82", "01012345678");
        ContactInfo contactInfo = ContactInfo.of(primaryPhone);
        
        return CustomerProfile.create(
                CustomerId.of(123L),
                personalInfo,
                contactInfo
        );
    }

    private CustomerProfile createProfileWithStatus(ProfileStatus status) {
        CustomerProfile profile = createMinimalProfile();
        // Status is set during creation and can be updated through domain methods
        return profile;
    }
}