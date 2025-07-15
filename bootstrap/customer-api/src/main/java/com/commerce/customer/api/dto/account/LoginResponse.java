package com.commerce.customer.api.dto.account;

import lombok.Getter;

@Getter
public class LoginResponse {
    private final String accessToken;
    private final String refreshToken;
    private final Long accountId;
    private final String email;
    
    public LoginResponse(String accessToken, String refreshToken, Long accountId, String email) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accountId = accountId;
        this.email = email;
    }
}