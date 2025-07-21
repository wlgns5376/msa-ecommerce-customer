package com.commerce.customer.api.config;

import com.commerce.customer.core.domain.event.DomainEventPublisher;
import com.commerce.infrastructure.kafka.event.KafkaEventPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

@TestConfiguration
@Profile("integration")
public class TestKafkaConfig {
    
    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> mockTemplate = mock(KafkaTemplate.class);
        CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
        future.complete(null);
        when(mockTemplate.send(anyString(), any())).thenReturn(future);
        when(mockTemplate.send(anyString(), anyString(), any())).thenReturn(future);
        return mockTemplate;
    }
    
    @Bean
    @Primary
    public ProducerFactory<String, Object> producerFactory() {
        return mock(ProducerFactory.class);
    }
    
    @Bean
    @Primary
    public KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }
    
    @Bean
    @Primary
    public DomainEventPublisher domainEventPublisher() {
        DomainEventPublisher mockPublisher = mock(DomainEventPublisher.class);
        doNothing().when(mockPublisher).publishAccountCreatedEvent(any());
        doNothing().when(mockPublisher).publishAccountActivatedEvent(any());
        return mockPublisher;
    }
}