package com.commerce.customer.core.domain.event;

public interface DomainEventPublisher {
    void publishAccountCreatedEvent(AccountCreatedEvent event);
    void publishAccountActivatedEvent(AccountActivatedEvent event);
}