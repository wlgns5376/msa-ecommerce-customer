package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;

@Getter
public class AccountId {
    private final Long value;

    private AccountId(Long value) {
        // null은 새로 생성되는 도메인 객체에서만 허용 (persistence 계층에서 ID 할당 전)
        if (value != null && value <= 0) {
            throw new IllegalArgumentException("계정 ID는 양수여야 합니다.");
        }
        this.value = value;
    }

    /**
     * 새로운 Account 생성 시 사용 (ID는 persistence 계층에서 할당)
     */
    public static AccountId newInstance() {
        return new AccountId(null);
    }

    /**
     * 기존 Account ID로 인스턴스 생성 (persistence에서 복원 시 사용)
     */
    public static AccountId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("기존 계정 ID는 null일 수 없습니다.");
        }
        return new AccountId(value);
    }

    /**
     * ID가 할당되었는지 확인
     */
    public boolean isAssigned() {
        return value != null;
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