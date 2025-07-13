package com.commerce.customer.core.domain.model.jwt;

import lombok.Getter;

import java.util.Objects;

@Getter
public class TokenPair {
    private final JwtToken accessToken;
    private final JwtToken refreshToken;

    private TokenPair(JwtToken accessToken, JwtToken refreshToken) {
        this.accessToken = Objects.requireNonNull(accessToken, "액세스 토큰은 필수입니다.");
        this.refreshToken = Objects.requireNonNull(refreshToken, "리프레시 토큰은 필수입니다.");
        
        if (accessToken.getType() != JwtTokenType.ACCESS) {
            throw new IllegalArgumentException("첫 번째 토큰은 액세스 토큰이어야 합니다.");
        }
        if (refreshToken.getType() != JwtTokenType.REFRESH) {
            throw new IllegalArgumentException("두 번째 토큰은 리프레시 토큰이어야 합니다.");
        }
    }

    public static TokenPair of(JwtToken accessToken, JwtToken refreshToken) {
        return new TokenPair(accessToken, refreshToken);
    }

    public boolean isAccessTokenExpired() {
        return accessToken.isExpired();
    }

    public boolean isRefreshTokenExpired() {
        return refreshToken.isExpired();
    }

    public boolean isValid() {
        return !isRefreshTokenExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TokenPair tokenPair = (TokenPair) o;
        return Objects.equals(accessToken, tokenPair.accessToken) &&
               Objects.equals(refreshToken, tokenPair.refreshToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessToken, refreshToken);
    }
}