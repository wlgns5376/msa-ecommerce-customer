package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TokenGeneratedEvent {
    private final CustomerId customerId;
    private final AccountId accountId;
    private final JwtTokenType tokenType;
    private final LocalDateTime occurredAt;

    public TokenGeneratedEvent(CustomerId customerId, AccountId accountId, JwtTokenType tokenType) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.tokenType = tokenType;
        this.occurredAt = LocalDateTime.now();
    }
}