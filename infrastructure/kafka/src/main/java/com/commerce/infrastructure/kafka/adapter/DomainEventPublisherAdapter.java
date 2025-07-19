package com.commerce.infrastructure.kafka.adapter;

import com.commerce.customer.core.domain.event.AccountActivatedEvent;
import com.commerce.customer.core.domain.event.AccountCreatedEvent;
import com.commerce.customer.core.domain.event.DomainEventPublisher;
import com.commerce.infrastructure.kafka.event.KafkaEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DomainEventPublisherAdapter implements DomainEventPublisher {
    
    private final KafkaEventPublisher kafkaEventPublisher;
    
    @Override
    public void publishAccountCreatedEvent(AccountCreatedEvent event) {
        kafkaEventPublisher.publishAccountCreatedEvent(event);
    }
    
    @Override
    public void publishAccountActivatedEvent(AccountActivatedEvent event) {
        kafkaEventPublisher.publishAccountActivatedEvent(event);
    }
}