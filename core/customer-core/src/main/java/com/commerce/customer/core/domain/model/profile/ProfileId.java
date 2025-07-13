package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class ProfileId {
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    private final Long value;

    private ProfileId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("프로필 ID는 양수여야 합니다.");
        }
        this.value = value;
    }

    public static ProfileId generate() {
        return new ProfileId(SEQUENCE.getAndIncrement());
    }

    public static ProfileId of(Long value) {
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
        return String.valueOf(value);
    }
}