package com.commerce.customer.core.application.usecase.account;

import com.commerce.customer.core.domain.model.AccountId;
import lombok.Getter;

@Getter
public class ActivateAccountUseCase {
    private final AccountId accountId;
    private final String activationCode;

    public ActivateAccountUseCase(AccountId accountId, String activationCode) {
        this.accountId = accountId;
        this.activationCode = activationCode;
    }
}