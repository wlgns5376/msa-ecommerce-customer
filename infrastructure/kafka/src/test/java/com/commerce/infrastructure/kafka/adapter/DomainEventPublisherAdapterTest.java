package com.commerce.infrastructure.kafka.adapter;

import com.commerce.customer.core.domain.event.AccountActivatedEvent;
import com.commerce.customer.core.domain.event.AccountCreatedEvent;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.infrastructure.kafka.event.KafkaEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherAdapterTest {

    @Mock
    private KafkaEventPublisher kafkaEventPublisher;

    private DomainEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DomainEventPublisherAdapter(kafkaEventPublisher);
    }

    @Test
    @DisplayName("계정 생성 이벤트를 Kafka로 발행한다")
    void publishAccountCreatedEvent_ShouldDelegateToKafkaEventPublisher() {
        // given
        AccountCreatedEvent event = new AccountCreatedEvent(
                AccountId.of(123L),
                CustomerId.of(123L),
                Email.of("test@example.com"),
                "ACTIVATE123"
        );

        // when
        adapter.publishAccountCreatedEvent(event);

        // then
        verify(kafkaEventPublisher).publishAccountCreatedEvent(event);
        verifyNoMoreInteractions(kafkaEventPublisher);
    }

    @Test
    @DisplayName("계정 활성화 이벤트를 Kafka로 발행한다")
    void publishAccountActivatedEvent_ShouldDelegateToKafkaEventPublisher() {
        // given
        AccountActivatedEvent event = AccountActivatedEvent.of(
                AccountId.of(456L),
                CustomerId.of(456L)
        );

        // when
        adapter.publishAccountActivatedEvent(event);

        // then
        verify(kafkaEventPublisher).publishAccountActivatedEvent(event);
        verifyNoMoreInteractions(kafkaEventPublisher);
    }

    @Test
    @DisplayName("여러 이벤트를 순차적으로 발행한다")
    void publishMultipleEvents_ShouldDelegateAllToKafkaEventPublisher() {
        // given
        AccountCreatedEvent createdEvent = new AccountCreatedEvent(
                AccountId.of(789L),
                CustomerId.of(789L),
                Email.of("multi@example.com"),
                "ACTIVATE789"
        );

        AccountActivatedEvent activatedEvent = AccountActivatedEvent.of(
                AccountId.of(789L),
                CustomerId.of(789L)
        );

        // when
        adapter.publishAccountCreatedEvent(createdEvent);
        adapter.publishAccountActivatedEvent(activatedEvent);

        // then
        verify(kafkaEventPublisher).publishAccountCreatedEvent(createdEvent);
        verify(kafkaEventPublisher).publishAccountActivatedEvent(activatedEvent);
        verifyNoMoreInteractions(kafkaEventPublisher);
    }
}