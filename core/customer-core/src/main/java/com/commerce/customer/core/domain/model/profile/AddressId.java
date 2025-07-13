package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class AddressId {
    private final String value;

    private AddressId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("주소 ID는 필수값입니다.");
        }
        this.value = value;
    }

    public static AddressId generate() {
        return new AddressId(UUID.randomUUID().toString());
    }

    public static AddressId of(String value) {
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
        return value;
    }
}