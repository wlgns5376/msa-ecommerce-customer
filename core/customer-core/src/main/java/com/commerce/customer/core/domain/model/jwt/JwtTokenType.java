package com.commerce.customer.core.domain.model.jwt;

import lombok.Getter;

@Getter
public enum JwtTokenType {
    ACCESS("액세스 토큰", 15), // 15분
    REFRESH("리프레시 토큰", 7 * 24 * 60); // 7일을 분 단위로

    private final String displayName;
    private final int expirationMinutes;

    JwtTokenType(String displayName, int expirationMinutes) {
        this.displayName = displayName;
        this.expirationMinutes = expirationMinutes;
    }
}