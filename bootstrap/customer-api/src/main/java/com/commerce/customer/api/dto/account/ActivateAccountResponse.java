package com.commerce.customer.api.dto.account;

import lombok.Getter;

@Getter
public class ActivateAccountResponse {
    private final String message;
    private final boolean activated;

    public ActivateAccountResponse(String message, boolean activated) {
        this.message = message;
        this.activated = activated;
    }

    public static ActivateAccountResponse success() {
        return new ActivateAccountResponse("계정이 성공적으로 활성화되었습니다.", true);
    }
}