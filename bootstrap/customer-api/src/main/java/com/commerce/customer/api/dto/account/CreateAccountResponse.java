package com.commerce.customer.api.dto.account;

import lombok.Getter;

@Getter
public class CreateAccountResponse {
    private final Long accountId;
    private final String message;
    
    public CreateAccountResponse(Long accountId, String message) {
        this.accountId = accountId;
        this.message = message;
    }
}