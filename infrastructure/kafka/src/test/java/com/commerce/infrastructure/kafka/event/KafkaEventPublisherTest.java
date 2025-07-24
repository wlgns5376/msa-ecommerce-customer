package com.commerce.infrastructure.kafka.event;

import com.commerce.customer.core.domain.event.AccountActivatedEvent;
import com.commerce.customer.core.domain.event.AccountCreatedEvent;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private SendResult<String, Object> sendResult;

    @Mock
    private RecordMetadata recordMetadata;

    private KafkaEventPublisher kafkaEventPublisher;

    @BeforeEach
    void setUp() {
        kafkaEventPublisher = new KafkaEventPublisher(kafkaTemplate);
    }

    @Test
    @DisplayName("계정 생성 이벤트를 성공적으로 발행한다")
    void publishAccountCreatedEvent_ShouldPublishSuccessfully() {
        // given
        AccountCreatedEvent event = new AccountCreatedEvent(
                AccountId.of(123L),
                CustomerId.of(123L),
                Email.of("test@example.com"),
                "ACTIVATE123"
        );

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.offset()).thenReturn(100L);

        // when
        kafkaEventPublisher.publishAccountCreatedEvent(event);
        future.complete(sendResult);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("customer.account.created");
        assertThat(keyCaptor.getValue()).isEqualTo("123");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("계정 생성 이벤트 발행 실패 시 에러를 로깅한다")
    void publishAccountCreatedEvent_ShouldLogError_WhenPublishFails() {
        // given
        AccountCreatedEvent event = new AccountCreatedEvent(
                AccountId.of(123L),
                CustomerId.of(123L),
                Email.of("test@example.com"),
                "ACTIVATE123"
        );

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // when
        kafkaEventPublisher.publishAccountCreatedEvent(event);
        future.completeExceptionally(new RuntimeException("Kafka connection failed"));

        // then
        verify(kafkaTemplate).send("customer.account.created", "123", event);
    }

    @Test
    @DisplayName("계정 활성화 이벤트를 성공적으로 발행한다")
    void publishAccountActivatedEvent_ShouldPublishSuccessfully() {
        // given
        AccountActivatedEvent event = AccountActivatedEvent.of(
                AccountId.of(456L),
                CustomerId.of(456L)
        );

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        when(recordMetadata.offset()).thenReturn(200L);

        // when
        kafkaEventPublisher.publishAccountActivatedEvent(event);
        future.complete(sendResult);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertThat(topicCaptor.getValue()).isEqualTo("customer.account.activated");
        assertThat(keyCaptor.getValue()).isEqualTo("456");
        assertThat(eventCaptor.getValue()).isEqualTo(event);
    }

    @Test
    @DisplayName("계정 활성화 이벤트 발행 실패 시 에러를 로깅한다")
    void publishAccountActivatedEvent_ShouldLogError_WhenPublishFails() {
        // given
        AccountActivatedEvent event = AccountActivatedEvent.of(
                AccountId.of(456L),
                CustomerId.of(456L)
        );

        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // when
        kafkaEventPublisher.publishAccountActivatedEvent(event);
        future.completeExceptionally(new RuntimeException("Kafka broker unavailable"));

        // then
        verify(kafkaTemplate).send("customer.account.activated", "456", event);
    }

    @Test
    @DisplayName("여러 이벤트를 동시에 발행할 수 있다")
    void publishMultipleEvents_ShouldPublishAllEventsIndependently() {
        // given
        AccountCreatedEvent createdEvent = new AccountCreatedEvent(
                AccountId.of(789L),
                CustomerId.of(789L),
                Email.of("multi@example.com"),
                "ACTIVATE789"
        );

        AccountActivatedEvent activatedEvent = AccountActivatedEvent.of(
                AccountId.of(790L),
                CustomerId.of(790L)
        );

        CompletableFuture<SendResult<String, Object>> future1 = new CompletableFuture<>();
        CompletableFuture<SendResult<String, Object>> future2 = new CompletableFuture<>();

        when(kafkaTemplate.send(eq("customer.account.created"), anyString(), any()))
                .thenReturn(future1);
        when(kafkaTemplate.send(eq("customer.account.activated"), anyString(), any()))
                .thenReturn(future2);

        // when
        kafkaEventPublisher.publishAccountCreatedEvent(createdEvent);
        kafkaEventPublisher.publishAccountActivatedEvent(activatedEvent);

        // then
        verify(kafkaTemplate).send("customer.account.created", "789", createdEvent);
        verify(kafkaTemplate).send("customer.account.activated", "790", activatedEvent);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("이벤트의 key는 accountId의 문자열 표현이다")
    void publishEvent_ShouldUseAccountIdAsKey() {
        // given
        AccountId accountId1 = AccountId.of(999L);
        AccountId accountId2 = AccountId.of(1000L);

        AccountCreatedEvent event1 = new AccountCreatedEvent(
                accountId1,
                CustomerId.of(999L),
                Email.of("key1@example.com"),
                "ACTIVATE999"
        );

        AccountActivatedEvent event2 = AccountActivatedEvent.of(
                accountId2,
                CustomerId.of(1000L)
        );

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenReturn(new CompletableFuture<>());

        // when
        kafkaEventPublisher.publishAccountCreatedEvent(event1);
        kafkaEventPublisher.publishAccountActivatedEvent(event2);

        // then
        verify(kafkaTemplate).send(anyString(), eq("999"), any());
        verify(kafkaTemplate).send(anyString(), eq("1000"), any());
    }
}