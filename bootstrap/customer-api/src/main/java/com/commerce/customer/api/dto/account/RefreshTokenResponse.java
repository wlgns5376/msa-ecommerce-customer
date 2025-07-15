package com.commerce.customer.api.dto.account;

import lombok.Getter;

@Getter
public class RefreshTokenResponse {
    private final String accessToken;
    private final String refreshToken;
    
    public RefreshTokenResponse(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }
}