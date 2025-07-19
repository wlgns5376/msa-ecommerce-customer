package com.commerce.infrastructure.kafka.event;

import com.commerce.customer.core.domain.event.AccountActivatedEvent;
import com.commerce.customer.core.domain.event.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ACCOUNT_CREATED_TOPIC = "customer.account.created";
    private static final String ACCOUNT_ACTIVATED_TOPIC = "customer.account.activated";

    public void publishAccountCreatedEvent(AccountCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(ACCOUNT_CREATED_TOPIC, event.getAccountId().getValue().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("계정 생성 이벤트 발행 성공: accountId={}, topic={}, offset={}",
                    event.getAccountId().getValue(), 
                    ACCOUNT_CREATED_TOPIC,
                    result.getRecordMetadata().offset());
            } else {
                log.error("계정 생성 이벤트 발행 실패: accountId={}", 
                    event.getAccountId().getValue(), ex);
            }
        });
    }

    public void publishAccountActivatedEvent(AccountActivatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(ACCOUNT_ACTIVATED_TOPIC, event.getAccountId().getValue().toString(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("계정 활성화 이벤트 발행 성공: accountId={}, topic={}, offset={}",
                    event.getAccountId().getValue(), 
                    ACCOUNT_ACTIVATED_TOPIC,
                    result.getRecordMetadata().offset());
            } else {
                log.error("계정 활성화 이벤트 발행 실패: accountId={}", 
                    event.getAccountId().getValue(), ex);
            }
        });
    }
}