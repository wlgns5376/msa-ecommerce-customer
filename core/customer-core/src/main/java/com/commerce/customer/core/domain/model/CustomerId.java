package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class CustomerId {
    private final String value;

    private CustomerId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("고객 ID는 필수값입니다.");
        }
        this.value = value;
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID().toString());
    }

    public static CustomerId of(String value) {
        return new CustomerId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerId that = (CustomerId) o;
        return Objects.equals(value, that.value);
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