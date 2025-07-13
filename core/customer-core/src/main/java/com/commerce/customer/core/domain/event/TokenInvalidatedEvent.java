package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TokenInvalidatedEvent {
    private final CustomerId customerId;
    private final JwtTokenType tokenType;
    private final String reason;
    private final LocalDateTime occurredAt;

    public TokenInvalidatedEvent(CustomerId customerId, JwtTokenType tokenType, String reason) {
        this.customerId = customerId;
        this.tokenType = tokenType;
        this.reason = reason;
        this.occurredAt = LocalDateTime.now();
    }
}