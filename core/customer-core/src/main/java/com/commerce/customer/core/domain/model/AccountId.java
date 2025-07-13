package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class AccountId {
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    
    private final Long value;

    private AccountId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("계정 ID는 양수여야 합니다.");
        }
        this.value = value;
    }

    public static AccountId generate() {
        return new AccountId(SEQUENCE.getAndIncrement());
    }

    public static AccountId of(Long value) {
        return new AccountId(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountId accountId = (AccountId) o;
        return Objects.equals(value, accountId.value);
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