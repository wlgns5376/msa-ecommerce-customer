package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;

@Getter
public class ContactInfo {
    private final PhoneNumber primaryPhone;
    private final PhoneNumber secondaryPhone;
    private final PhoneNumber emergencyPhone;

    private ContactInfo(PhoneNumber primaryPhone, PhoneNumber secondaryPhone, PhoneNumber emergencyPhone) {
        this.primaryPhone = Objects.requireNonNull(primaryPhone, "주 연락처는 필수값입니다.");
        this.secondaryPhone = secondaryPhone; // nullable
        this.emergencyPhone = emergencyPhone; // nullable
    }

    public static ContactInfo of(PhoneNumber primaryPhone) {
        return new ContactInfo(primaryPhone, null, null);
    }

    public static ContactInfo of(PhoneNumber primaryPhone, PhoneNumber secondaryPhone) {
        return new ContactInfo(primaryPhone, secondaryPhone, null);
    }

    public static ContactInfo of(PhoneNumber primaryPhone, PhoneNumber secondaryPhone, PhoneNumber emergencyPhone) {
        return new ContactInfo(primaryPhone, secondaryPhone, emergencyPhone);
    }

    public ContactInfo updatePrimaryPhone(PhoneNumber newPrimaryPhone) {
        return new ContactInfo(newPrimaryPhone, this.secondaryPhone, this.emergencyPhone);
    }

    public ContactInfo updateSecondaryPhone(PhoneNumber newSecondaryPhone) {
        return new ContactInfo(this.primaryPhone, newSecondaryPhone, this.emergencyPhone);
    }

    public ContactInfo updateEmergencyPhone(PhoneNumber newEmergencyPhone) {
        return new ContactInfo(this.primaryPhone, this.secondaryPhone, newEmergencyPhone);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactInfo that = (ContactInfo) o;
        return Objects.equals(primaryPhone, that.primaryPhone) &&
               Objects.equals(secondaryPhone, that.secondaryPhone) &&
               Objects.equals(emergencyPhone, that.emergencyPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryPhone, secondaryPhone, emergencyPhone);
    }

    @Override
    public String toString() {
        return "ContactInfo{" +
                "primaryPhone=" + primaryPhone +
                ", secondaryPhone=" + secondaryPhone +
                ", emergencyPhone=" + emergencyPhone +
                '}';
    }
}