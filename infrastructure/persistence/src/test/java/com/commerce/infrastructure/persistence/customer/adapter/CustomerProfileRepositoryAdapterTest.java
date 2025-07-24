package com.commerce.infrastructure.persistence.customer.adapter;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.*;
import com.commerce.infrastructure.persistence.customer.entity.CustomerProfileEntity;
import com.commerce.infrastructure.persistence.customer.mapper.CustomerProfileMapper;
import com.commerce.infrastructure.persistence.customer.repository.CustomerProfileJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileRepositoryAdapterTest {

    @Mock
    private CustomerProfileJpaRepository customerProfileJpaRepository;

    @Mock
    private CustomerProfileMapper customerProfileMapper;

    private CustomerProfileRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CustomerProfileRepositoryAdapter(customerProfileJpaRepository, customerProfileMapper);
    }

    @Test
    @DisplayName("새로운 프로필을 저장한다")
    void save_ShouldCreateNewProfile_WhenProfileDoesNotExist() {
        // given
        CustomerProfile profile = createTestProfile();
        CustomerProfileEntity entity = createTestEntity();
        CustomerProfileEntity savedEntity = CustomerProfileEntity.builder()
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

        when(customerProfileJpaRepository.findByCustomerId(123L)).thenReturn(Optional.empty());
        when(customerProfileMapper.toEntity(profile)).thenReturn(entity);
        when(customerProfileJpaRepository.save(entity)).thenReturn(savedEntity);
        when(customerProfileMapper.toDomain(savedEntity)).thenReturn(profile);

        // when
        CustomerProfile result = adapter.save(profile);

        // then
        assertThat(result).isEqualTo(profile);
        verify(customerProfileJpaRepository).findByCustomerId(123L);
        verify(customerProfileMapper).toEntity(profile);
        verify(customerProfileJpaRepository).save(entity);
        verify(customerProfileMapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("기존 프로필을 업데이트한다")
    void save_ShouldUpdateExistingProfile_WhenProfileExists() {
        // given
        CustomerProfile profile = createTestProfile();
        CustomerProfileEntity existingEntity = createTestEntity();
        CustomerProfileEntity savedEntity = CustomerProfileEntity.builder()
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

        when(customerProfileJpaRepository.findByCustomerId(123L)).thenReturn(Optional.of(existingEntity));
        when(customerProfileJpaRepository.save(existingEntity)).thenReturn(savedEntity);
        when(customerProfileMapper.toDomain(savedEntity)).thenReturn(profile);

        // when
        CustomerProfile result = adapter.save(profile);

        // then
        assertThat(result).isEqualTo(profile);
        verify(customerProfileJpaRepository).findByCustomerId(123L);
        verify(customerProfileMapper, never()).toEntity(any());
        verify(customerProfileJpaRepository).save(existingEntity);
        
        // Verify entity update methods were called
        verify(existingEntity).updatePersonalInfo(anyString(), anyString(), any(), any(), any());
        verify(existingEntity).updateContactInfo(anyString(), any());
        verify(existingEntity).updateMarketingConsent(anyBoolean(), anyBoolean(), anyBoolean());
        verify(existingEntity).updateNotificationSettings(anyBoolean(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    @DisplayName("ProfileId로 프로필을 조회한다")
    void findById_ShouldReturnProfile_WhenExists() {
        // given
        ProfileId profileId = ProfileId.of(123L);
        CustomerProfileEntity entity = createTestEntity();
        CustomerProfile profile = createTestProfile();

        when(customerProfileJpaRepository.findById(123L)).thenReturn(Optional.of(entity));
        when(customerProfileMapper.toDomain(entity)).thenReturn(profile);

        // when
        Optional<CustomerProfile> result = adapter.findById(profileId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(profile);
        verify(customerProfileJpaRepository).findById(123L);
        verify(customerProfileMapper).toDomain(entity);
    }

    @Test
    @DisplayName("ProfileId로 조회 시 프로필이 없으면 빈 Optional을 반환한다")
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // given
        ProfileId profileId = ProfileId.of(123L);

        when(customerProfileJpaRepository.findById(123L)).thenReturn(Optional.empty());

        // when
        Optional<CustomerProfile> result = adapter.findById(profileId);

        // then
        assertThat(result).isEmpty();
        verify(customerProfileJpaRepository).findById(123L);
        verify(customerProfileMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("CustomerId로 프로필을 조회한다")
    void findByCustomerId_ShouldReturnProfile_WhenExists() {
        // given
        CustomerId customerId = CustomerId.of(123L);
        CustomerProfileEntity entity = createTestEntity();
        CustomerProfile profile = createTestProfile();

        when(customerProfileJpaRepository.findByCustomerId(123L)).thenReturn(Optional.of(entity));
        when(customerProfileMapper.toDomain(entity)).thenReturn(profile);

        // when
        Optional<CustomerProfile> result = adapter.findByCustomerId(customerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(profile);
        verify(customerProfileJpaRepository).findByCustomerId(123L);
        verify(customerProfileMapper).toDomain(entity);
    }

    @Test
    @DisplayName("CustomerId로 프로필 존재 여부를 확인한다")
    void existsByCustomerId_ShouldReturnTrue_WhenExists() {
        // given
        CustomerId customerId = CustomerId.of(123L);

        when(customerProfileJpaRepository.existsByCustomerId(123L)).thenReturn(true);

        // when
        boolean result = adapter.existsByCustomerId(customerId);

        // then
        assertThat(result).isTrue();
        verify(customerProfileJpaRepository).existsByCustomerId(123L);
    }

    @Test
    @DisplayName("프로필을 삭제한다")
    void delete_ShouldDeleteProfile() {
        // given
        CustomerProfile profile = createTestProfileWithId();

        // when
        adapter.delete(profile);

        // then
        verify(customerProfileJpaRepository).deleteById(123L);
    }

    @Test
    @DisplayName("CustomerId로 활성 프로필을 조회한다")
    void findActiveByCustomerId_ShouldReturnActiveProfile() {
        // given
        CustomerId customerId = CustomerId.of(123L);
        CustomerProfileEntity entity = createTestEntity();
        CustomerProfile profile = createTestProfile();

        when(customerProfileJpaRepository.findActiveProfileByCustomerId(123L))
                .thenReturn(Optional.of(entity));
        when(customerProfileMapper.toDomain(entity)).thenReturn(profile);

        // when
        Optional<CustomerProfile> result = adapter.findActiveByCustomerId(customerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(profile);
        verify(customerProfileJpaRepository).findActiveProfileByCustomerId(123L);
        verify(customerProfileMapper).toDomain(entity);
    }

    @Test
    @DisplayName("CustomerId로 주소를 포함한 프로필을 조회한다")
    void findByCustomerIdWithAddresses_ShouldReturnProfileWithAddresses() {
        // given
        CustomerId customerId = CustomerId.of(123L);
        CustomerProfileEntity entity = createTestEntity();
        CustomerProfile profile = createTestProfile();

        when(customerProfileJpaRepository.findByCustomerIdWithAddresses(123L))
                .thenReturn(Optional.of(entity));
        when(customerProfileMapper.toDomain(entity)).thenReturn(profile);

        // when
        Optional<CustomerProfile> result = adapter.findByCustomerIdWithAddresses(customerId);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(profile);
        verify(customerProfileJpaRepository).findByCustomerIdWithAddresses(123L);
        verify(customerProfileMapper).toDomain(entity);
    }

    @Test
    @DisplayName("ProfileId로 프로필을 삭제한다")
    void deleteById_ShouldDeleteProfile() {
        // given
        ProfileId profileId = ProfileId.of(123L);

        // when
        adapter.deleteById(profileId);

        // then
        verify(customerProfileJpaRepository).deleteById(123L);
    }

    @Test
    @DisplayName("프로필 업데이트 시 모든 필드가 정확히 업데이트된다")
    void save_ShouldUpdateAllFields_WhenUpdatingExistingProfile() {
        // given
        CustomerProfile profile = createFullProfile();
        CustomerProfileEntity existingEntity = mock(CustomerProfileEntity.class);
        CustomerProfileEntity savedEntity = createTestEntity();

        when(customerProfileJpaRepository.findByCustomerId(123L)).thenReturn(Optional.of(existingEntity));
        when(customerProfileJpaRepository.save(existingEntity)).thenReturn(savedEntity);
        when(customerProfileMapper.toDomain(savedEntity)).thenReturn(profile);

        // when
        adapter.save(profile);

        // then
        ArgumentCaptor<String> firstNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> lastNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> birthDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<CustomerProfileEntity.Gender> genderCaptor = ArgumentCaptor.forClass(CustomerProfileEntity.Gender.class);
        
        verify(existingEntity).updatePersonalInfo(
                firstNameCaptor.capture(),
                lastNameCaptor.capture(),
                birthDateCaptor.capture(),
                genderCaptor.capture(),
                isNull()
        );
        
        assertThat(firstNameCaptor.getValue()).isEqualTo("John");
        assertThat(lastNameCaptor.getValue()).isEqualTo("Doe");
        assertThat(birthDateCaptor.getValue()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(genderCaptor.getValue()).isEqualTo(CustomerProfileEntity.Gender.MALE);

        ArgumentCaptor<String> primaryPhoneCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> secondaryPhoneCaptor = ArgumentCaptor.forClass(String.class);
        
        verify(existingEntity).updateContactInfo(
                primaryPhoneCaptor.capture(),
                secondaryPhoneCaptor.capture()
        );
        
        assertThat(primaryPhoneCaptor.getValue()).isEqualTo("01012345678");
        assertThat(secondaryPhoneCaptor.getValue()).isEqualTo("01087654321");

        verify(existingEntity).updateMarketingConsent(true, true, false);
        verify(existingEntity).updateNotificationSettings(true, false, true, true);
    }

    @Test
    @DisplayName("선택적 필드가 null인 경우에도 정상적으로 업데이트한다")
    void save_ShouldHandleNullOptionalFields_WhenUpdating() {
        // given
        CustomerProfile profile = createMinimalProfile();
        CustomerProfileEntity existingEntity = mock(CustomerProfileEntity.class);
        CustomerProfileEntity savedEntity = createTestEntity();

        when(customerProfileJpaRepository.findByCustomerId(123L)).thenReturn(Optional.of(existingEntity));
        when(customerProfileJpaRepository.save(existingEntity)).thenReturn(savedEntity);
        when(customerProfileMapper.toDomain(savedEntity)).thenReturn(profile);

        // when
        adapter.save(profile);

        // then
        verify(existingEntity).updatePersonalInfo(
                eq("John"),
                eq("Doe"),
                isNull(),
                isNull(),
                isNull()
        );
        
        verify(existingEntity).updateContactInfo(
                eq("01012345678"),
                isNull()
        );
    }

    // Helper methods
    private CustomerProfile createTestProfile() {
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

    private CustomerProfile createTestProfileWithId() {
        CustomerProfile profile = createTestProfile();
        // Use reflection to set ProfileId for testing
        try {
            java.lang.reflect.Field profileIdField = CustomerProfile.class.getDeclaredField("profileId");
            profileIdField.setAccessible(true);
            profileIdField.set(profile, ProfileId.of(123L));
        } catch (Exception e) {
            throw new RuntimeException("Failed to set profileId", e);
        }
        return profile;
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
                        .build()
        );
        
        return profile;
    }

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

    private CustomerProfileEntity createTestEntity() {
        CustomerProfileEntity entity = CustomerProfileEntity.builder()
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
        
        // Entity is a builder-created object, not a mock
        
        return entity;
    }
}