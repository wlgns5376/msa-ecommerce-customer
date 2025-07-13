package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class ProfileId {
    private final String value;

    private ProfileId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("프로필 ID는 필수값입니다.");
        }
        this.value = value;
    }

    public static ProfileId generate() {
        return new ProfileId(UUID.randomUUID().toString());
    }

    public static ProfileId of(String value) {
        return new ProfileId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProfileId profileId = (ProfileId) o;
        return Objects.equals(value, profileId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}