package com.commerce.customer.api.dto.account;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActivateAccountRequest {
    @NotBlank(message = "인증 코드는 필수입니다.")
    private String activationCode;

    public ActivateAccountRequest(String activationCode) {
        this.activationCode = activationCode;
    }
}