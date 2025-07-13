package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountCreatedEvent {
    private final AccountId accountId;
    private final CustomerId customerId;
    private final Email email;
    private final LocalDateTime occurredAt;

    public AccountCreatedEvent(AccountId accountId, CustomerId customerId, Email email) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.email = email;
        this.occurredAt = LocalDateTime.now();
    }
}