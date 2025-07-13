package com.commerce.customer.core.domain.model.profile;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Getter
@Builder(toBuilder = true)
public class NotificationSettings {
    private final boolean emailNotification;
    private final boolean smsNotification;
    private final boolean pushNotification;
    private final boolean orderUpdates;
    private final boolean promotionalOffers;

    private NotificationSettings(boolean emailNotification, boolean smsNotification, 
                               boolean pushNotification, boolean orderUpdates, boolean promotionalOffers) {
        this.emailNotification = emailNotification;
        this.smsNotification = smsNotification;
        this.pushNotification = pushNotification;
        this.orderUpdates = orderUpdates;
        this.promotionalOffers = promotionalOffers;
    }

    public static NotificationSettings getDefault() {
        return NotificationSettings.builder()
            .emailNotification(true)
            .smsNotification(true)
            .pushNotification(true)
            .orderUpdates(true)
            .promotionalOffers(false)
            .build();
    }

    public NotificationSettings updateEmailNotification(boolean emailNotification) {
        return this.toBuilder().emailNotification(emailNotification).build();
    }

    public NotificationSettings updateSmsNotification(boolean smsNotification) {
        return this.toBuilder().smsNotification(smsNotification).build();
    }

    public NotificationSettings updatePushNotification(boolean pushNotification) {
        return this.toBuilder().pushNotification(pushNotification).build();
    }

    public NotificationSettings updateOrderUpdates(boolean orderUpdates) {
        return this.toBuilder().orderUpdates(orderUpdates).build();
    }

    public NotificationSettings updatePromotionalOffers(boolean promotionalOffers) {
        return this.toBuilder().promotionalOffers(promotionalOffers).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationSettings that = (NotificationSettings) o;
        return emailNotification == that.emailNotification &&
               smsNotification == that.smsNotification &&
               pushNotification == that.pushNotification &&
               orderUpdates == that.orderUpdates &&
               promotionalOffers == that.promotionalOffers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(emailNotification, smsNotification, pushNotification, orderUpdates, promotionalOffers);
    }

    @Override
    public String toString() {
        return "NotificationSettings{" +
                "emailNotification=" + emailNotification +
                ", smsNotification=" + smsNotification +
                ", pushNotification=" + pushNotification +
                ", orderUpdates=" + orderUpdates +
                ", promotionalOffers=" + promotionalOffers +
                '}';
    }
}