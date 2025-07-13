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
                .primaryPhone(profile.getContactInfo().getPrimaryPhone().getValue())
                .status(mapToEntityStatus(profile.getStatus()));

        // Optional fields
        if (profile.getPersonalInfo().getBirthDate() != null) {
            builder.birthDate(profile.getPersonalInfo().getBirthDate().getValue());
        }
        
        if (profile.getPersonalInfo().getGender() != null) {
            builder.gender(mapToEntityGender(profile.getPersonalInfo().getGender()));
        }
        
        if (profile.getPersonalInfo().getProfileImage() != null) {
            builder.profileImageUrl(profile.getPersonalInfo().getProfileImage().getUrl());
        }
        
        if (profile.getContactInfo().getSecondaryPhone() != null) {
            builder.secondaryPhone(profile.getContactInfo().getSecondaryPhone().getValue());
        }

        // Marketing consent
        MarketingConsent marketingConsent = profile.getPreferences().getMarketingConsent();
        builder.emailMarketingConsent(marketingConsent.isEmailConsent())
               .smsMarketingConsent(marketingConsent.isSmsConsent())
               .pushMarketingConsent(marketingConsent.isPushConsent());

        // Notification settings
        NotificationSettings notificationSettings = profile.getPreferences().getNotificationSettings();
        builder.orderNotifications(notificationSettings.isOrderNotifications())
               .promotionNotifications(notificationSettings.isPromotionNotifications())
               .accountNotifications(notificationSettings.isAccountNotifications())
               .reviewNotifications(notificationSettings.isReviewNotifications());

        return builder.build();
    }

    public CustomerProfile toDomain(CustomerProfileEntity entity) {
        if (entity == null) {
            return null;
        }

        // Personal Info 구성
        FullName fullName = FullName.of(entity.getFirstName(), entity.getLastName());
        PersonalInfo.PersonalInfoBuilder personalInfoBuilder = PersonalInfo.builder()
                .fullName(fullName);

        if (entity.getBirthDate() != null) {
            personalInfoBuilder.birthDate(BirthDate.of(entity.getBirthDate()));
        }
        
        if (entity.getGender() != null) {
            personalInfoBuilder.gender(mapToDomainGender(entity.getGender()));
        }
        
        if (entity.getProfileImageUrl() != null) {
            personalInfoBuilder.profileImage(ProfileImage.of(entity.getProfileImageUrl()));
        }

        PersonalInfo personalInfo = personalInfoBuilder.build();

        // Contact Info 구성
        ContactInfo.ContactInfoBuilder contactInfoBuilder = ContactInfo.builder()
                .primaryPhone(PhoneNumber.of(entity.getPrimaryPhone()));
        
        if (entity.getSecondaryPhone() != null) {
            contactInfoBuilder.secondaryPhone(PhoneNumber.of(entity.getSecondaryPhone()));
        }
        
        ContactInfo contactInfo = contactInfoBuilder.build();

        // Marketing Consent 구성
        MarketingConsent marketingConsent = MarketingConsent.builder()
                .emailConsent(entity.getEmailMarketingConsent())
                .smsConsent(entity.getSmsMarketingConsent())
                .pushConsent(entity.getPushMarketingConsent())
                .build();

        // Notification Settings 구성
        NotificationSettings notificationSettings = NotificationSettings.builder()
                .orderNotifications(entity.getOrderNotifications())
                .promotionNotifications(entity.getPromotionNotifications())
                .accountNotifications(entity.getAccountNotifications())
                .reviewNotifications(entity.getReviewNotifications())
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

        return CustomerProfile.builder()
                .profileId(ProfileId.of(entity.getProfileId()))
                .customerId(CustomerId.of(entity.getCustomerId()))
                .personalInfo(personalInfo)
                .contactInfo(contactInfo)
                .addresses(addresses)
                .preferences(preferences)
                .status(mapToDomainStatus(entity.getStatus()))
                .build();
    }

    private Address mapAddressToDomain(AddressEntity entity) {
        return Address.create(
                AddressId.of(entity.getAddressId()),
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
                entity.getBrandName(),
                mapToDomainPreferenceLevel(entity.getPreferenceLevel())
        );
    }

    private CategoryInterest mapCategoryInterestToDomain(CategoryInterestEntity entity) {
        return CategoryInterest.of(
                entity.getCategoryName(),
                mapToDomainInterestLevel(entity.getInterestLevel())
        );
    }

    // Enum 매핑 메서드들
    private CustomerProfileEntity.Gender mapToEntityGender(Gender gender) {
        return switch (gender) {
            case MALE -> CustomerProfileEntity.Gender.MALE;
            case FEMALE -> CustomerProfileEntity.Gender.FEMALE;
            case OTHER -> CustomerProfileEntity.Gender.OTHER;
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