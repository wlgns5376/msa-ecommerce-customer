package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AccountActivatedEvent {
    private final AccountId accountId;
    private final CustomerId customerId;
    private final LocalDateTime occurredAt;

    public AccountActivatedEvent(AccountId accountId, CustomerId customerId) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.occurredAt = LocalDateTime.now();
    }
}