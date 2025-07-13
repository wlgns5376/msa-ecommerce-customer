package com.commerce.customer.core.domain.model.jwt;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class JwtToken {
    private final String value;
    private final JwtTokenType type;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;

    private JwtToken(String value, JwtTokenType type, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT 토큰 값은 필수입니다.");
        }
        if (type == null) {
            throw new IllegalArgumentException("JWT 토큰 타입은 필수입니다.");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("발급 시간은 필수입니다.");
        }
        if (expiresAt == null) {
            throw new IllegalArgumentException("만료 시간은 필수입니다.");
        }
        if (expiresAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("만료 시간은 발급 시간보다 이후여야 합니다.");
        }

        this.value = value;
        this.type = type;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public static JwtToken of(String value, JwtTokenType type, LocalDateTime issuedAt, LocalDateTime expiresAt) {
        return new JwtToken(value, type, issuedAt, expiresAt);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtToken jwtToken = (JwtToken) o;
        return Objects.equals(value, jwtToken.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "JwtToken{type=" + type + ", issuedAt=" + issuedAt + ", expiresAt=" + expiresAt + "}";
    }
}