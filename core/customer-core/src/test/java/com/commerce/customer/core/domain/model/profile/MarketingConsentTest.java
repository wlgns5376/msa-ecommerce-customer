package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MarketingConsent 값객체 테스트")
class MarketingConsentTest {

    @Test
    @DisplayName("기본 MarketingConsent를 생성할 수 있다")
    void createDefaultMarketingConsent() {
        // When
        MarketingConsent consent = MarketingConsent.getDefault();

        // Then
        assertThat(consent).isNotNull();
        assertThat(consent.isEmailMarketing()).isFalse();
        assertThat(consent.isSmsMarketing()).isFalse();
        assertThat(consent.isPersonalizedAds()).isFalse();
        assertThat(consent.getConsentDate()).isNotNull();
        assertThat(consent.getLastUpdated()).isNotNull();
        assertThat(consent.hasAnyConsent()).isFalse();
    }

    @Test
    @DisplayName("빌더를 사용하여 MarketingConsent를 생성할 수 있다")
    void createMarketingConsentWithBuilder() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        MarketingConsent consent = MarketingConsent.builder()
            .emailMarketing(true)
            .smsMarketing(false)
            .personalizedAds(true)
            .consentDate(now)
            .lastUpdated(now)
            .build();

        // Then
        assertThat(consent).isNotNull();
        assertThat(consent.isEmailMarketing()).isTrue();
        assertThat(consent.isSmsMarketing()).isFalse();
        assertThat(consent.isPersonalizedAds()).isTrue();
        assertThat(consent.getConsentDate()).isEqualTo(now);
        assertThat(consent.getLastUpdated()).isEqualTo(now);
        assertThat(consent.hasAnyConsent()).isTrue();
    }

    @Test
    @DisplayName("모든 마케팅 동의를 한번에 설정할 수 있다")
    void giveConsentForAll() {
        // Given
        MarketingConsent original = MarketingConsent.getDefault();

        // When
        MarketingConsent updated = original.giveConsent(true, true, true);

        // Then
        assertThat(updated.isEmailMarketing()).isTrue();
        assertThat(updated.isSmsMarketing()).isTrue();
        assertThat(updated.isPersonalizedAds()).isTrue();
        assertThat(updated.hasAnyConsent()).isTrue();
        assertThat(updated.getLastUpdated()).isAfter(original.getLastUpdated());
    }

    @Test
    @DisplayName("일부 마케팅 동의만 설정할 수 있다")
    void givePartialConsent() {
        // Given
        MarketingConsent original = MarketingConsent.getDefault();

        // When
        MarketingConsent updated = original.giveConsent(true, false, true);

        // Then
        assertThat(updated.isEmailMarketing()).isTrue();
        assertThat(updated.isSmsMarketing()).isFalse();
        assertThat(updated.isPersonalizedAds()).isTrue();
        assertThat(updated.hasAnyConsent()).isTrue();
    }

    @Test
    @DisplayName("모든 마케팅 동의를 철회할 수 있다")
    void revokeAllConsent() {
        // Given
        MarketingConsent original = MarketingConsent.builder()
            .emailMarketing(true)
            .smsMarketing(true)
            .personalizedAds(true)
            .consentDate(LocalDateTime.now())
            .lastUpdated(LocalDateTime.now())
            .build();

        // When
        MarketingConsent revoked = original.revokeConsent();

        // Then
        assertThat(revoked.isEmailMarketing()).isFalse();
        assertThat(revoked.isSmsMarketing()).isFalse();
        assertThat(revoked.isPersonalizedAds()).isFalse();
        assertThat(revoked.hasAnyConsent()).isFalse();
        assertThat(revoked.getLastUpdated()).isAfter(original.getLastUpdated());
    }

    @Test
    @DisplayName("이메일 마케팅 동의만 업데이트할 수 있다")
    void updateEmailConsent() {
        // Given
        MarketingConsent original = MarketingConsent.getDefault();

        // When
        MarketingConsent updated = original.updateEmailConsent(true);

        // Then
        assertThat(updated.isEmailMarketing()).isTrue();
        assertThat(updated.isSmsMarketing()).isFalse(); // 기존 값 유지
        assertThat(updated.isPersonalizedAds()).isFalse(); // 기존 값 유지
        assertThat(updated.hasAnyConsent()).isTrue();
        assertThat(updated.getLastUpdated()).isAfter(original.getLastUpdated());
    }

    @Test
    @DisplayName("SMS 마케팅 동의만 업데이트할 수 있다")
    void updateSmsConsent() {
        // Given
        MarketingConsent original = MarketingConsent.getDefault();

        // When
        MarketingConsent updated = original.updateSmsConsent(true);

        // Then
        assertThat(updated.isEmailMarketing()).isFalse(); // 기존 값 유지
        assertThat(updated.isSmsMarketing()).isTrue();
        assertThat(updated.isPersonalizedAds()).isFalse(); // 기존 값 유지
        assertThat(updated.hasAnyConsent()).isTrue();
        assertThat(updated.getLastUpdated()).isAfter(original.getLastUpdated());
    }

    @Test
    @DisplayName("개인화 광고 동의만 업데이트할 수 있다")
    void updatePersonalizedAdsConsent() {
        // Given
        MarketingConsent original = MarketingConsent.getDefault();

        // When
        MarketingConsent updated = original.updatePersonalizedAdsConsent(true);

        // Then
        assertThat(updated.isEmailMarketing()).isFalse(); // 기존 값 유지
        assertThat(updated.isSmsMarketing()).isFalse(); // 기존 값 유지
        assertThat(updated.isPersonalizedAds()).isTrue();
        assertThat(updated.hasAnyConsent()).isTrue();
        assertThat(updated.getLastUpdated()).isAfter(original.getLastUpdated());
    }

    @Test
    @DisplayName("hasAnyConsent는 하나라도 동의한 경우 true를 반환한다")
    void hasAnyConsentReturnsTrueWhenAnyConsentIsGiven() {
        // Given & When & Then
        MarketingConsent emailOnly = MarketingConsent.getDefault().updateEmailConsent(true);
        assertThat(emailOnly.hasAnyConsent()).isTrue();

        MarketingConsent smsOnly = MarketingConsent.getDefault().updateSmsConsent(true);
        assertThat(smsOnly.hasAnyConsent()).isTrue();

        MarketingConsent adsOnly = MarketingConsent.getDefault().updatePersonalizedAdsConsent(true);
        assertThat(adsOnly.hasAnyConsent()).isTrue();
    }

    @Test
    @DisplayName("hasAnyConsent는 모든 동의가 false인 경우 false를 반환한다")
    void hasAnyConsentReturnsFalseWhenNoConsentIsGiven() {
        // Given
        MarketingConsent noConsent = MarketingConsent.getDefault();

        // When & Then
        assertThat(noConsent.hasAnyConsent()).isFalse();
    }

    @Test
    @DisplayName("빌더에서 consentDate와 lastUpdated가 null인 경우 현재 시간으로 설정된다")
    void builderSetsCurrentTimeWhenDatesAreNull() {
        // Given
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);

        // When
        MarketingConsent consent = MarketingConsent.builder()
            .emailMarketing(true)
            .smsMarketing(false)
            .personalizedAds(false)
            .consentDate(null)
            .lastUpdated(null)
            .build();

        // Then
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertThat(consent.getConsentDate()).isAfter(before).isBefore(after);
        assertThat(consent.getLastUpdated()).isAfter(before).isBefore(after);
    }

    @Test
    @DisplayName("동일한 정보를 가진 MarketingConsent는 같다고 판단된다")
    void equalityTest() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        MarketingConsent consent1 = MarketingConsent.builder()
            .emailMarketing(true)
            .smsMarketing(false)
            .personalizedAds(true)
            .consentDate(now)
            .lastUpdated(now)
            .build();

        MarketingConsent consent2 = MarketingConsent.builder()
            .emailMarketing(true)
            .smsMarketing(false)
            .personalizedAds(true)
            .consentDate(now)
            .lastUpdated(now)
            .build();

        // When & Then
        assertThat(consent1).isEqualTo(consent2);
        assertThat(consent1.hashCode()).isEqualTo(consent2.hashCode());
    }

    @Test
    @DisplayName("다른 정보를 가진 MarketingConsent는 다르다고 판단된다")
    void inequalityTest() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        MarketingConsent consent1 = MarketingConsent.builder()
            .emailMarketing(true)
            .smsMarketing(false)
            .personalizedAds(true)
            .consentDate(now)
            .lastUpdated(now)
            .build();

        MarketingConsent consent2 = MarketingConsent.builder()
            .emailMarketing(false)
            .smsMarketing(true)
            .personalizedAds(true)
            .consentDate(now)
            .lastUpdated(now)
            .build();

        // When & Then
        assertThat(consent1).isNotEqualTo(consent2);
    }

    @Test
    @DisplayName("toString 메서드가 올바르게 동작한다")
    void toStringTest() {
        // Given
        MarketingConsent consent = MarketingConsent.getDefault();

        // When
        String result = consent.toString();

        // Then
        assertThat(result).contains("MarketingConsent");
        assertThat(result).contains("emailMarketing=false");
        assertThat(result).contains("smsMarketing=false");
        assertThat(result).contains("personalizedAds=false");
        assertThat(result).contains("consentDate=");
        assertThat(result).contains("lastUpdated=");
    }
}