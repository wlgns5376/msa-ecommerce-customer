package com.commerce.infrastructure.persistence.customer.entity;

import com.commerce.infrastructure.persistence.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "brand_preferences", 
       indexes = {
           @Index(name = "idx_brand_preference_profile_id", columnList = "profile_id"),
           @Index(name = "idx_brand_preference_brand_name", columnList = "brand_name")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_profile_brand", columnNames = {"profile_id", "brand_name"})
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandPreferenceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_preference_id")
    private Long brandPreferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private CustomerProfileEntity customerProfile;

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(name = "preference_level", nullable = false, length = 20)
    private PreferenceLevel preferenceLevel;

    @Builder
    public BrandPreferenceEntity(CustomerProfileEntity customerProfile, String brandName, 
                               PreferenceLevel preferenceLevel) {
        this.customerProfile = customerProfile;
        this.brandName = brandName;
        this.preferenceLevel = preferenceLevel;
    }

    public void updatePreferenceLevel(PreferenceLevel preferenceLevel) {
        this.preferenceLevel = preferenceLevel;
    }

    // JPA를 위한 setter (package-private)
    void setCustomerProfile(CustomerProfileEntity customerProfile) {
        this.customerProfile = customerProfile;
    }

    public enum PreferenceLevel {
        LOVE, LIKE, DISLIKE
    }
}