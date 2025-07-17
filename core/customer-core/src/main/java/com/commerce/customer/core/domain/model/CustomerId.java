package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class CustomerId {
    
    private static final AtomicLong TEST_SEQUENCE = new AtomicLong(1);
    
    private final Long value;

    private CustomerId(Long value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("고객 ID는 양수여야 합니다.");
        }
        this.value = value;
    }


    public static CustomerId of(Long value) {
        return new CustomerId(value);
    }
    
    /**
     * 테스트용 ID 생성 메서드
     * @deprecated 테스트에서만 사용해야 합니다. 실제 운영 환경에서는 Repository를 통해 생성해야 합니다.
     */
    @Deprecated
    public static CustomerId generate() {
        return new CustomerId(TEST_SEQUENCE.getAndIncrement());
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