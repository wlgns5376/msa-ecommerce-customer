package com.commerce.customer.api.dto.profile;

import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class ProfileResponse {
    private Long profileId;
    private Long customerId;
    private PersonalInfoDto personalInfo;
    private ContactInfoDto contactInfo;
    private List<AddressDto> addresses;
    private MarketingConsentDto marketingConsent;
    private NotificationSettingsDto notificationSettings;
    
    @Getter
    @Builder
    public static class PersonalInfoDto {
        private String firstName;
        private String lastName;
        private LocalDate birthDate;
        private String gender;
        private String profileImageUrl;
    }
    
    @Getter
    @Builder
    public static class ContactInfoDto {
        private String phoneNumber;
        private String secondaryPhoneNumber;
    }
    
    @Getter
    @Builder
    public static class AddressDto {
        private Long addressId;
        private String type;
        private String alias;
        private String zipCode;
        private String roadAddress;
        private String jibunAddress;
        private String detailAddress;
        private boolean isDefault;
    }
    
    @Getter
    @Builder
    public static class MarketingConsentDto {
        private boolean emailConsent;
        private boolean smsConsent;
        private boolean pushConsent;
    }
    
    @Getter
    @Builder
    public static class NotificationSettingsDto {
        private boolean orderNotifications;
        private boolean promotionNotifications;
        private boolean accountNotifications;
        private boolean reviewNotifications;
    }
    
    public static ProfileResponse from(CustomerProfile profile) {
        return ProfileResponse.builder()
                .profileId(profile.getProfileId().getValue())
                .customerId(profile.getCustomerId().getValue())
                .personalInfo(PersonalInfoDto.builder()
                        .firstName(profile.getPersonalInfo().getFullName().getFirstName())
                        .lastName(profile.getPersonalInfo().getFullName().getLastName())
                        .birthDate(profile.getPersonalInfo().getBirthDate() != null ? 
                                profile.getPersonalInfo().getBirthDate().getDate() : null)
                        .gender(profile.getPersonalInfo().getGender() != null ? 
                                profile.getPersonalInfo().getGender().name() : null)
                        .profileImageUrl(null) // PersonalInfo에 profileImageUrl 필드가 없음
                        .build())
                .contactInfo(ContactInfoDto.builder()
                        .phoneNumber(profile.getContactInfo().getPrimaryPhone() != null ? 
                                profile.getContactInfo().getPrimaryPhone().getNumber() : null)
                        .secondaryPhoneNumber(profile.getContactInfo().getSecondaryPhone() != null ? 
                                profile.getContactInfo().getSecondaryPhone().getNumber() : null)
                        .build())
                .addresses(profile.getAddresses().stream()
                        .map(address -> AddressDto.builder()
                                .addressId(address.getAddressId().getValue())
                                .type(address.getType().name())
                                .alias(address.getAlias())
                                .zipCode(address.getZipCode())
                                .roadAddress(address.getRoadAddress())
                                .jibunAddress(address.getJibunAddress())
                                .detailAddress(address.getDetailAddress())
                                .isDefault(address.isDefault())
                                .build())
                        .collect(Collectors.toList()))
                .marketingConsent(MarketingConsentDto.builder()
                        .emailConsent(profile.getPreferences().getMarketingConsent().isEmailMarketing())
                        .smsConsent(profile.getPreferences().getMarketingConsent().isSmsMarketing())
                        .pushConsent(profile.getPreferences().getMarketingConsent().isPersonalizedAds())
                        .build())
                .notificationSettings(NotificationSettingsDto.builder()
                        .orderNotifications(profile.getPreferences().getNotificationSettings().isOrderUpdates())
                        .promotionNotifications(profile.getPreferences().getNotificationSettings().isPromotionalOffers())
                        .accountNotifications(profile.getPreferences().getNotificationSettings().isEmailNotification())
                        .reviewNotifications(profile.getPreferences().getNotificationSettings().isPushNotification())
                        .build())
                .build();
    }
}