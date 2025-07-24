package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountActivatedEvent 테스트")
class AccountActivatedEventTest {

    @Test
    @DisplayName("계정 활성화 이벤트 생성")
    void createAccountActivatedEvent() {
        // given
        AccountId accountId = AccountId.of(1L);
        CustomerId customerId = CustomerId.of(12345L);
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        AccountActivatedEvent event = new AccountActivatedEvent(accountId, customerId);

        // then
        assertThat(event.getAccountId()).isEqualTo(accountId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("팩토리 메서드를 통한 이벤트 생성")
    void createAccountActivatedEventWithFactory() {
        // given
        AccountId accountId = AccountId.of(1L);
        CustomerId customerId = CustomerId.of(12345L);

        // when
        AccountActivatedEvent event = AccountActivatedEvent.of(accountId, customerId);

        // then
        assertThat(event.getAccountId()).isEqualTo(accountId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getOccurredAt()).isNotNull();
    }
}