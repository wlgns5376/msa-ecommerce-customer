package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class LoginSuccessfulEvent {
    private final AccountId accountId;
    private final CustomerId customerId;
    private final LocalDateTime loginAt;
    private final LocalDateTime occurredAt;

    public LoginSuccessfulEvent(AccountId accountId, CustomerId customerId, LocalDateTime loginAt) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.loginAt = loginAt;
        this.occurredAt = LocalDateTime.now();
    }
}