package com.commerce.infrastructure.persistence.customer.entity;

import com.commerce.infrastructure.persistence.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_profiles", indexes = {
    @Index(name = "idx_profile_customer_id", columnList = "customer_id", unique = true),
    @Index(name = "idx_profile_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerProfileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    // Personal Info
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    // Contact Info
    @Column(name = "primary_phone", nullable = false, length = 20)
    private String primaryPhone;

    @Column(name = "secondary_phone", length = 20)
    private String secondaryPhone;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProfileStatus status;

    // Marketing Consent
    @Column(name = "email_marketing_consent", nullable = false)
    private Boolean emailMarketingConsent = false;

    @Column(name = "sms_marketing_consent", nullable = false)
    private Boolean smsMarketingConsent = false;

    @Column(name = "push_marketing_consent", nullable = false)
    private Boolean pushMarketingConsent = false;

    // Notification Settings
    @Column(name = "order_notifications", nullable = false)
    private Boolean orderNotifications = true;

    @Column(name = "promotion_notifications", nullable = false)
    private Boolean promotionNotifications = true;

    @Column(name = "account_notifications", nullable = false)
    private Boolean accountNotifications = true;

    @Column(name = "review_notifications", nullable = false)
    private Boolean reviewNotifications = true;

    // Addresses - OneToMany relationship
    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AddressEntity> addresses = new ArrayList<>();

    // Brand Preferences - OneToMany relationship
    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<BrandPreferenceEntity> brandPreferences = new ArrayList<>();

    // Category Interests - OneToMany relationship
    @OneToMany(mappedBy = "customerProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<CategoryInterestEntity> categoryInterests = new ArrayList<>();

    @Builder
    public CustomerProfileEntity(Long customerId, String firstName, String lastName,
                               LocalDate birthDate, Gender gender, String profileImageUrl,
                               String primaryPhone, String secondaryPhone, ProfileStatus status,
                               Boolean emailMarketingConsent, Boolean smsMarketingConsent, Boolean pushMarketingConsent,
                               Boolean orderNotifications, Boolean promotionNotifications, 
                               Boolean accountNotifications, Boolean reviewNotifications) {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
        this.status = status != null ? status : ProfileStatus.ACTIVE;
        this.emailMarketingConsent = emailMarketingConsent != null ? emailMarketingConsent : false;
        this.smsMarketingConsent = smsMarketingConsent != null ? smsMarketingConsent : false;
        this.pushMarketingConsent = pushMarketingConsent != null ? pushMarketingConsent : false;
        this.orderNotifications = orderNotifications != null ? orderNotifications : true;
        this.promotionNotifications = promotionNotifications != null ? promotionNotifications : true;
        this.accountNotifications = accountNotifications != null ? accountNotifications : true;
        this.reviewNotifications = reviewNotifications != null ? reviewNotifications : true;
    }

    // Personal Info 업데이트
    public void updatePersonalInfo(String firstName, String lastName, LocalDate birthDate, 
                                 Gender gender, String profileImageUrl) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.profileImageUrl = profileImageUrl;
    }

    // Contact Info 업데이트
    public void updateContactInfo(String primaryPhone, String secondaryPhone) {
        this.primaryPhone = primaryPhone;
        this.secondaryPhone = secondaryPhone;
    }

    // Marketing Consent 업데이트
    public void updateMarketingConsent(Boolean emailConsent, Boolean smsConsent, Boolean pushConsent) {
        this.emailMarketingConsent = emailConsent;
        this.smsMarketingConsent = smsConsent;
        this.pushMarketingConsent = pushConsent;
    }

    // Notification Settings 업데이트
    public void updateNotificationSettings(Boolean orderNotifications, Boolean promotionNotifications,
                                         Boolean accountNotifications, Boolean reviewNotifications) {
        this.orderNotifications = orderNotifications;
        this.promotionNotifications = promotionNotifications;
        this.accountNotifications = accountNotifications;
        this.reviewNotifications = reviewNotifications;
    }

    // Status 업데이트
    public void updateStatus(ProfileStatus status) {
        this.status = status;
    }

    // Helper methods for associations
    public void addAddress(AddressEntity address) {
        addresses.add(address);
        address.setCustomerProfile(this);
    }

    public void removeAddress(AddressEntity address) {
        addresses.remove(address);
        address.setCustomerProfile(null);
    }

    public void addBrandPreference(BrandPreferenceEntity brandPreference) {
        brandPreferences.add(brandPreference);
        brandPreference.setCustomerProfile(this);
    }

    public void removeBrandPreference(BrandPreferenceEntity brandPreference) {
        brandPreferences.remove(brandPreference);
        brandPreference.setCustomerProfile(null);
    }

    public void addCategoryInterest(CategoryInterestEntity categoryInterest) {
        categoryInterests.add(categoryInterest);
        categoryInterest.setCustomerProfile(this);
    }

    public void removeCategoryInterest(CategoryInterestEntity categoryInterest) {
        categoryInterests.remove(categoryInterest);
        categoryInterest.setCustomerProfile(null);
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum ProfileStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}