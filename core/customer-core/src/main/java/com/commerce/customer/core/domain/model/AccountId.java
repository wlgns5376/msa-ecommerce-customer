package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class AccountId {
    private final String value;

    private AccountId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("계정 ID는 필수값입니다.");
        }
        this.value = value;
    }

    public static AccountId generate() {
        return new AccountId(UUID.randomUUID().toString());
    }

    public static AccountId of(String value) {
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
        return value;
    }
}