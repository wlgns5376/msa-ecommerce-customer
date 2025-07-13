package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class AddressId {
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    private final Long value;

    private AddressId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("주소 ID는 양수여야 합니다.");
        }
        this.value = value;
    }

    public static AddressId generate() {
        return new AddressId(SEQUENCE.getAndIncrement());
    }

    public static AddressId of(Long value) {
        return new AddressId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressId addressId = (AddressId) o;
        return Objects.equals(value, addressId.value);
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