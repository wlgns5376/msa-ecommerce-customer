package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("NotificationSettings 값객체 테스트")
class NotificationSettingsTest {

    @Test
    @DisplayName("기본 NotificationSettings를 생성할 수 있다")
    void createDefaultNotificationSettings() {
        // When
        NotificationSettings settings = NotificationSettings.getDefault();

        // Then
        assertThat(settings).isNotNull();
        assertThat(settings.isEmailNotification()).isTrue();
        assertThat(settings.isSmsNotification()).isTrue();
        assertThat(settings.isPushNotification()).isTrue();
        assertThat(settings.isOrderUpdates()).isTrue();
        assertThat(settings.isPromotionalOffers()).isFalse();
    }

    @Test
    @DisplayName("빌더를 사용하여 NotificationSettings를 생성할 수 있다")
    void createNotificationSettingsWithBuilder() {
        // When
        NotificationSettings settings = NotificationSettings.builder()
            .emailNotification(false)
            .smsNotification(true)
            .pushNotification(false)
            .orderUpdates(true)
            .promotionalOffers(true)
            .build();

        // Then
        assertThat(settings).isNotNull();
        assertThat(settings.isEmailNotification()).isFalse();
        assertThat(settings.isSmsNotification()).isTrue();
        assertThat(settings.isPushNotification()).isFalse();
        assertThat(settings.isOrderUpdates()).isTrue();
        assertThat(settings.isPromotionalOffers()).isTrue();
    }

    @Test
    @DisplayName("이메일 알림 설정을 업데이트할 수 있다")
    void updateEmailNotification() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original.updateEmailNotification(false);

        // Then
        assertThat(updated.isEmailNotification()).isFalse();
        assertThat(updated.isSmsNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isPushNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isOrderUpdates()).isTrue(); // 기존 값 유지
        assertThat(updated.isPromotionalOffers()).isFalse(); // 기존 값 유지
    }

    @Test
    @DisplayName("SMS 알림 설정을 업데이트할 수 있다")
    void updateSmsNotification() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original.updateSmsNotification(false);

        // Then
        assertThat(updated.isEmailNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isSmsNotification()).isFalse();
        assertThat(updated.isPushNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isOrderUpdates()).isTrue(); // 기존 값 유지
        assertThat(updated.isPromotionalOffers()).isFalse(); // 기존 값 유지
    }

    @Test
    @DisplayName("푸시 알림 설정을 업데이트할 수 있다")
    void updatePushNotification() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original.updatePushNotification(false);

        // Then
        assertThat(updated.isEmailNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isSmsNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isPushNotification()).isFalse();
        assertThat(updated.isOrderUpdates()).isTrue(); // 기존 값 유지
        assertThat(updated.isPromotionalOffers()).isFalse(); // 기존 값 유지
    }

    @Test
    @DisplayName("주문 업데이트 알림 설정을 업데이트할 수 있다")
    void updateOrderUpdates() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original.updateOrderUpdates(false);

        // Then
        assertThat(updated.isEmailNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isSmsNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isPushNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isOrderUpdates()).isFalse();
        assertThat(updated.isPromotionalOffers()).isFalse(); // 기존 값 유지
    }

    @Test
    @DisplayName("프로모션 알림 설정을 업데이트할 수 있다")
    void updatePromotionalOffers() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original.updatePromotionalOffers(true);

        // Then
        assertThat(updated.isEmailNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isSmsNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isPushNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isOrderUpdates()).isTrue(); // 기존 값 유지
        assertThat(updated.isPromotionalOffers()).isTrue();
    }

    @Test
    @DisplayName("연속적으로 설정을 업데이트할 수 있다")
    void updateMultipleSettings() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original
            .updateEmailNotification(false)
            .updateSmsNotification(false)
            .updatePromotionalOffers(true);

        // Then
        assertThat(updated.isEmailNotification()).isFalse();
        assertThat(updated.isSmsNotification()).isFalse();
        assertThat(updated.isPushNotification()).isTrue(); // 기존 값 유지
        assertThat(updated.isOrderUpdates()).isTrue(); // 기존 값 유지
        assertThat(updated.isPromotionalOffers()).isTrue();
    }

    @Test
    @DisplayName("모든 알림을 비활성화할 수 있다")
    void disableAllNotifications() {
        // Given
        NotificationSettings original = NotificationSettings.getDefault();

        // When
        NotificationSettings updated = original
            .updateEmailNotification(false)
            .updateSmsNotification(false)
            .updatePushNotification(false)
            .updateOrderUpdates(false)
            .updatePromotionalOffers(false);

        // Then
        assertThat(updated.isEmailNotification()).isFalse();
        assertThat(updated.isSmsNotification()).isFalse();
        assertThat(updated.isPushNotification()).isFalse();
        assertThat(updated.isOrderUpdates()).isFalse();
        assertThat(updated.isPromotionalOffers()).isFalse();
    }

    @Test
    @DisplayName("모든 알림을 활성화할 수 있다")
    void enableAllNotifications() {
        // Given
        NotificationSettings original = NotificationSettings.builder()
            .emailNotification(false)
            .smsNotification(false)
            .pushNotification(false)
            .orderUpdates(false)
            .promotionalOffers(false)
            .build();

        // When
        NotificationSettings updated = original
            .updateEmailNotification(true)
            .updateSmsNotification(true)
            .updatePushNotification(true)
            .updateOrderUpdates(true)
            .updatePromotionalOffers(true);

        // Then
        assertThat(updated.isEmailNotification()).isTrue();
        assertThat(updated.isSmsNotification()).isTrue();
        assertThat(updated.isPushNotification()).isTrue();
        assertThat(updated.isOrderUpdates()).isTrue();
        assertThat(updated.isPromotionalOffers()).isTrue();
    }

    @Test
    @DisplayName("동일한 설정을 가진 NotificationSettings는 같다고 판단된다")
    void equalityTest() {
        // Given
        NotificationSettings settings1 = NotificationSettings.builder()
            .emailNotification(true)
            .smsNotification(false)
            .pushNotification(true)
            .orderUpdates(false)
            .promotionalOffers(true)
            .build();

        NotificationSettings settings2 = NotificationSettings.builder()
            .emailNotification(true)
            .smsNotification(false)
            .pushNotification(true)
            .orderUpdates(false)
            .promotionalOffers(true)
            .build();

        // When & Then
        assertThat(settings1).isEqualTo(settings2);
        assertThat(settings1.hashCode()).isEqualTo(settings2.hashCode());
    }

    @Test
    @DisplayName("다른 설정을 가진 NotificationSettings는 다르다고 판단된다")
    void inequalityTest() {
        // Given
        NotificationSettings settings1 = NotificationSettings.getDefault();
        NotificationSettings settings2 = settings1.updateEmailNotification(false);

        // When & Then
        assertThat(settings1).isNotEqualTo(settings2);
    }

    @Test
    @DisplayName("toString 메서드가 올바르게 동작한다")
    void toStringTest() {
        // Given
        NotificationSettings settings = NotificationSettings.getDefault();

        // When
        String result = settings.toString();

        // Then
        assertThat(result).contains("NotificationSettings");
        assertThat(result).contains("emailNotification=true");
        assertThat(result).contains("smsNotification=true");
        assertThat(result).contains("pushNotification=true");
        assertThat(result).contains("orderUpdates=true");
        assertThat(result).contains("promotionalOffers=false");
    }
}