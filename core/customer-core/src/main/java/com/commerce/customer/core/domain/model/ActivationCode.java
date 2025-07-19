package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
public class ActivationCode {
    private final String code;
    private final LocalDateTime expiresAt;

    private ActivationCode(String code, LocalDateTime expiresAt) {
        this.code = Objects.requireNonNull(code, "인증 코드는 필수값입니다.");
        this.expiresAt = Objects.requireNonNull(expiresAt, "만료 시간은 필수값입니다.");
        validateCode(code);
    }

    public static ActivationCode generate() {
        String code = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32);
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // 24시간 유효
        return new ActivationCode(code, expiresAt);
    }

    public static ActivationCode of(String code, LocalDateTime expiresAt) {
        return new ActivationCode(code, expiresAt);
    }

    private void validateCode(String code) {
        if (code.isEmpty() || code.length() < 32) {
            throw new IllegalArgumentException("인증 코드는 32자 이상이어야 합니다.");
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean matches(String inputCode) {
        return this.code.equals(inputCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivationCode that = (ActivationCode) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}