package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class CustomerId {
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    
    private final Long value;

    private CustomerId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("고객 ID는 양수여야 합니다.");
        }
        this.value = value;
    }

    public static CustomerId generate() {
        return new CustomerId(SEQUENCE.getAndIncrement());
    }

    public static CustomerId of(Long value) {
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
        return value.toString();
    }
}