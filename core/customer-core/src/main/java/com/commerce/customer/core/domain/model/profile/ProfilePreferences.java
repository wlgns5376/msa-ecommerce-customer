package com.commerce.customer.core.domain.model.profile;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Builder(toBuilder = true)
public class ProfilePreferences {
    private final List<CategoryInterest> categoryInterests;
    private final List<BrandPreference> brandPreferences;
    private final NotificationSettings notificationSettings;
    private final MarketingConsent marketingConsent;

    private ProfilePreferences(List<CategoryInterest> categoryInterests, List<BrandPreference> brandPreferences,
                             NotificationSettings notificationSettings, MarketingConsent marketingConsent) {
        this.categoryInterests = categoryInterests != null ? new ArrayList<>(categoryInterests) : new ArrayList<>();
        this.brandPreferences = brandPreferences != null ? new ArrayList<>(brandPreferences) : new ArrayList<>();
        this.notificationSettings = notificationSettings != null ? notificationSettings : NotificationSettings.getDefault();
        this.marketingConsent = marketingConsent != null ? marketingConsent : MarketingConsent.getDefault();
        
        validateLimits();
    }

    public static ProfilePreferences getDefault() {
        return ProfilePreferences.builder()
            .categoryInterests(new ArrayList<>())
            .brandPreferences(new ArrayList<>())
            .notificationSettings(NotificationSettings.getDefault())
            .marketingConsent(MarketingConsent.getDefault())
            .build();
    }

    public ProfilePreferences updateCategoryInterests(List<CategoryInterest> categoryInterests) {
        return this.toBuilder().categoryInterests(categoryInterests).build();
    }

    public ProfilePreferences updateBrandPreferences(List<BrandPreference> brandPreferences) {
        return this.toBuilder().brandPreferences(brandPreferences).build();
    }

    public ProfilePreferences updateNotificationSettings(NotificationSettings notificationSettings) {
        return this.toBuilder().notificationSettings(notificationSettings).build();
    }

    public ProfilePreferences updateMarketingConsent(MarketingConsent marketingConsent) {
        return this.toBuilder().marketingConsent(marketingConsent).build();
    }

    private void validateLimits() {
        if (categoryInterests.size() > 20) {
            throw new IllegalArgumentException("카테고리 관심도는 최대 20개까지 설정 가능합니다.");
        }
        
        if (brandPreferences.size() > 50) {
            throw new IllegalArgumentException("브랜드 선호도는 최대 50개까지 설정 가능합니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfilePreferences that = (ProfilePreferences) o;
        return Objects.equals(categoryInterests, that.categoryInterests) &&
               Objects.equals(brandPreferences, that.brandPreferences) &&
               Objects.equals(notificationSettings, that.notificationSettings) &&
               Objects.equals(marketingConsent, that.marketingConsent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryInterests, brandPreferences, notificationSettings, marketingConsent);
    }

    @Override
    public String toString() {
        return "ProfilePreferences{" +
                "categoryInterests=" + categoryInterests +
                ", brandPreferences=" + brandPreferences +
                ", notificationSettings=" + notificationSettings +
                ", marketingConsent=" + marketingConsent +
                '}';
    }
}