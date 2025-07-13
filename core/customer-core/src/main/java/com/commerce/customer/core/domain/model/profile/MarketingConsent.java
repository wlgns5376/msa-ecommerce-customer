package com.commerce.customer.core.domain.model.profile;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Builder(toBuilder = true)
public class MarketingConsent {
    private final boolean emailMarketing;
    private final boolean smsMarketing;
    private final boolean personalizedAds;
    private final LocalDateTime consentDate;
    private final LocalDateTime lastUpdated;

    private MarketingConsent(boolean emailMarketing, boolean smsMarketing, boolean personalizedAds,
                           LocalDateTime consentDate, LocalDateTime lastUpdated) {
        this.emailMarketing = emailMarketing;
        this.smsMarketing = smsMarketing;
        this.personalizedAds = personalizedAds;
        this.consentDate = consentDate != null ? consentDate : LocalDateTime.now();
        this.lastUpdated = lastUpdated != null ? lastUpdated : LocalDateTime.now();
    }

    public static MarketingConsent getDefault() {
        LocalDateTime now = LocalDateTime.now();
        return MarketingConsent.builder()
            .emailMarketing(false)
            .smsMarketing(false)
            .personalizedAds(false)
            .consentDate(now)
            .lastUpdated(now)
            .build();
    }

    public MarketingConsent giveConsent(boolean emailMarketing, boolean smsMarketing, boolean personalizedAds) {
        return this.toBuilder()
            .emailMarketing(emailMarketing)
            .smsMarketing(smsMarketing)
            .personalizedAds(personalizedAds)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    public MarketingConsent revokeConsent() {
        return this.toBuilder()
            .emailMarketing(false)
            .smsMarketing(false)
            .personalizedAds(false)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    public MarketingConsent updateEmailConsent(boolean emailMarketing) {
        return this.toBuilder()
            .emailMarketing(emailMarketing)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    public MarketingConsent updateSmsConsent(boolean smsMarketing) {
        return this.toBuilder()
            .smsMarketing(smsMarketing)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    public MarketingConsent updatePersonalizedAdsConsent(boolean personalizedAds) {
        return this.toBuilder()
            .personalizedAds(personalizedAds)
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    public boolean hasAnyConsent() {
        return emailMarketing || smsMarketing || personalizedAds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketingConsent that = (MarketingConsent) o;
        return emailMarketing == that.emailMarketing &&
               smsMarketing == that.smsMarketing &&
               personalizedAds == that.personalizedAds &&
               Objects.equals(consentDate, that.consentDate) &&
               Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailMarketing, smsMarketing, personalizedAds, consentDate, lastUpdated);
    }

    @Override
    public String toString() {
        return "MarketingConsent{" +
                "emailMarketing=" + emailMarketing +
                ", smsMarketing=" + smsMarketing +
                ", personalizedAds=" + personalizedAds +
                ", consentDate=" + consentDate +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}