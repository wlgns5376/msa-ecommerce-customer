package com.commerce.infrastructure.persistence.customer.mapper;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.*;
import com.commerce.infrastructure.persistence.customer.entity.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomerProfileMapper {

    public CustomerProfileEntity toEntity(CustomerProfile profile) {
        if (profile == null) {
            return null;
        }

        CustomerProfileEntity.CustomerProfileEntityBuilder builder = CustomerProfileEntity.builder()
                .customerId(profile.getCustomerId().getValue())
                .firstName(profile.getPersonalInfo().getFullName().getFirstName())
                .lastName(profile.getPersonalInfo().getFullName().getLastName())
                .primaryPhone(profile.getContactInfo().getPrimaryPhone().getNumber())
                .status(mapToEntityStatus(profile.getStatus()));

        // Optional fields
        if (profile.getPersonalInfo().getBirthDate() != null) {
            builder.birthDate(profile.getPersonalInfo().getBirthDate().getDate());
        }
        
        if (profile.getPersonalInfo().getGender() != null) {
            builder.gender(mapToEntityGender(profile.getPersonalInfo().getGender()));
        }
        
        if (profile.getPersonalInfo().getProfileImage() != null) {
            builder.profileImageUrl(profile.getPersonalInfo().getProfileImage().getImageUrl());
        }
        
        if (profile.getContactInfo().getSecondaryPhone() != null) {
            builder.secondaryPhone(profile.getContactInfo().getSecondaryPhone().getNumber());
        }

        // Marketing consent
        MarketingConsent marketingConsent = profile.getPreferences().getMarketingConsent();
        builder.emailMarketingConsent(marketingConsent.isEmailMarketing())
               .smsMarketingConsent(marketingConsent.isSmsMarketing())
               .pushMarketingConsent(marketingConsent.isPersonalizedAds());

        // Notification settings
        NotificationSettings notificationSettings = profile.getPreferences().getNotificationSettings();
        builder.orderNotifications(notificationSettings.isOrderUpdates())
               .promotionNotifications(notificationSettings.isPromotionalOffers())
               .accountNotifications(notificationSettings.isEmailNotification())
               .reviewNotifications(notificationSettings.isSmsNotification());

        return builder.build();
    }

    public CustomerProfile toDomain(CustomerProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        // Personal Info 구성
        FullName fullName = FullName.of(entity.getFirstName(), entity.getLastName());
        
        BirthDate birthDate = entity.getBirthDate() != null ? 
            BirthDate.of(entity.getBirthDate()) : null;
        
        Gender gender = entity.getGender() != null ? 
            mapToDomainGender(entity.getGender()) : null;
        
        ProfileImage profileImage = entity.getProfileImageUrl() != null ? 
            ProfileImage.of(entity.getProfileImageUrl()) : null;

        PersonalInfo personalInfo = PersonalInfo.of(fullName, birthDate, gender, profileImage);

        // Contact Info 구성
        PhoneNumber primaryPhone = PhoneNumber.of("+82", entity.getPrimaryPhone());
        PhoneNumber secondaryPhone = entity.getSecondaryPhone() != null ? 
            PhoneNumber.of("+82", entity.getSecondaryPhone()) : null;
            
        ContactInfo contactInfo = secondaryPhone != null ? 
            ContactInfo.of(primaryPhone, secondaryPhone) : 
            ContactInfo.of(primaryPhone);

        // Marketing Consent 구성
        MarketingConsent marketingConsent = MarketingConsent.builder()
                .emailMarketing(entity.getEmailMarketingConsent())
                .smsMarketing(entity.getSmsMarketingConsent())
                .personalizedAds(entity.getPushMarketingConsent())
                .build();

        // Notification Settings 구성
        NotificationSettings notificationSettings = NotificationSettings.builder()
                .orderUpdates(entity.getOrderNotifications())
                .promotionalOffers(entity.getPromotionNotifications())
                .emailNotification(entity.getAccountNotifications())
                .smsNotification(entity.getReviewNotifications())
                .pushNotification(true) // default value
                .build();

        // Addresses 변환
        List<Address> addresses = new ArrayList<>();
        if (entity.getAddresses() != null) {
            for (AddressEntity addressEntity : entity.getAddresses()) {
                addresses.add(mapAddressToDomain(addressEntity));
            }
        }

        // Brand Preferences 변환
        List<BrandPreference> brandPreferences = new ArrayList<>();
        if (entity.getBrandPreferences() != null) {
            for (BrandPreferenceEntity brandEntity : entity.getBrandPreferences()) {
                brandPreferences.add(mapBrandPreferenceToDomain(brandEntity));
            }
        }

        // Category Interests 변환
        List<CategoryInterest> categoryInterests = new ArrayList<>();
        if (entity.getCategoryInterests() != null) {
            for (CategoryInterestEntity categoryEntity : entity.getCategoryInterests()) {
                categoryInterests.add(mapCategoryInterestToDomain(categoryEntity));
            }
        }

        // Profile Preferences 구성
        ProfilePreferences preferences = ProfilePreferences.builder()
                .marketingConsent(marketingConsent)
                .notificationSettings(notificationSettings)
                .categoryInterests(categoryInterests)
                .brandPreferences(brandPreferences)
                .build();

        // CustomerProfile 생성 (create 메서드 사용)
        return CustomerProfile.create(
                CustomerId.of(entity.getCustomerId()),
                personalInfo,
                contactInfo
        );
    }

    private Address mapAddressToDomain(AddressEntity entity) {
        return Address.create(
                mapToDomainAddressType(entity.getType()),
                entity.getAlias(),
                entity.getZipCode(),
                entity.getRoadAddress(),
                entity.getJibunAddress(),
                entity.getDetailAddress()
        );
    }

    private BrandPreference mapBrandPreferenceToDomain(BrandPreferenceEntity entity) {
        return BrandPreference.of(
                "BRAND_" + entity.getBrandName().toUpperCase(), // 임시 ID 생성
                entity.getBrandName(),
                mapToDomainPreferenceLevel(entity.getPreferenceLevel())
        );
    }

    private CategoryInterest mapCategoryInterestToDomain(CategoryInterestEntity entity) {
        return CategoryInterest.of(
                "CAT_" + entity.getCategoryName().toUpperCase(), // 임시 ID 생성
                entity.getCategoryName(),
                mapToDomainInterestLevel(entity.getInterestLevel())
        );
    }

    // Enum 매핑 메서드들
    private CustomerProfileEntity.Gender mapToEntityGender(Gender gender) {
        return switch (gender) {
            case MALE -> CustomerProfileEntity.Gender.MALE;
            case FEMALE -> CustomerProfileEntity.Gender.FEMALE;
            case OTHER, PREFER_NOT_TO_SAY -> CustomerProfileEntity.Gender.OTHER;
        };
    }

    private Gender mapToDomainGender(CustomerProfileEntity.Gender gender) {
        return switch (gender) {
            case MALE -> Gender.MALE;
            case FEMALE -> Gender.FEMALE;
            case OTHER -> Gender.OTHER;
        };
    }

    private CustomerProfileEntity.ProfileStatus mapToEntityStatus(ProfileStatus status) {
        return switch (status) {
            case ACTIVE -> CustomerProfileEntity.ProfileStatus.ACTIVE;
            case INACTIVE -> CustomerProfileEntity.ProfileStatus.INACTIVE;
            case SUSPENDED -> CustomerProfileEntity.ProfileStatus.SUSPENDED;
        };
    }

    private ProfileStatus mapToDomainStatus(CustomerProfileEntity.ProfileStatus status) {
        return switch (status) {
            case ACTIVE -> ProfileStatus.ACTIVE;
            case INACTIVE -> ProfileStatus.INACTIVE;
            case SUSPENDED -> ProfileStatus.SUSPENDED;
        };
    }

    private AddressType mapToDomainAddressType(AddressEntity.AddressType type) {
        return switch (type) {
            case HOME -> AddressType.HOME;
            case WORK -> AddressType.WORK;
            case OTHER -> AddressType.OTHER;
        };
    }

    private PreferenceLevel mapToDomainPreferenceLevel(BrandPreferenceEntity.PreferenceLevel level) {
        return switch (level) {
            case LOVE -> PreferenceLevel.LOVE;
            case LIKE -> PreferenceLevel.LIKE;
            case DISLIKE -> PreferenceLevel.DISLIKE;
        };
    }

    private InterestLevel mapToDomainInterestLevel(CategoryInterestEntity.InterestLevel level) {
        return switch (level) {
            case HIGH -> InterestLevel.HIGH;
            case MEDIUM -> InterestLevel.MEDIUM;
            case LOW -> InterestLevel.LOW;
        };
    }
}