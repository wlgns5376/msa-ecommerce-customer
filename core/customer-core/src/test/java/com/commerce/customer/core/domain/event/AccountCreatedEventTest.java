package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AccountCreatedEvent 도메인 이벤트 테스트")
class AccountCreatedEventTest {

    @Test
    @DisplayName("유효한 정보로 AccountCreatedEvent를 생성할 수 있다")
    void createAccountCreatedEventWithValidInfo() {
        // Given
        AccountId accountId = AccountId.of(123L);
        CustomerId customerId = CustomerId.generate();
        Email email = Email.of("test@example.com");
        LocalDateTime before = LocalDateTime.now();

        // When
        AccountCreatedEvent event = new AccountCreatedEvent(accountId, customerId, email);

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertThat(event).isNotNull();
        assertThat(event.getAccountId()).isEqualTo(accountId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getEmail()).isEqualTo(email);
        assertThat(event.getOccurredAt()).isAfter(before.minusSeconds(1)).isBefore(after.plusSeconds(1));
    }

    @Test
    @DisplayName("이벤트 생성 시 발생 시간이 자동으로 설정된다")
    void occurredAtIsSetAutomatically() {
        // Given
        AccountId accountId = AccountId.of(123L);
        CustomerId customerId = CustomerId.generate();
        Email email = Email.of("test@example.com");

        // When
        AccountCreatedEvent event = new AccountCreatedEvent(accountId, customerId, email);

        // Then
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("여러 이벤트를 생성할 때 각각 다른 발생 시간을 가진다")
    void multipleEventsHaveDifferentOccurredTimes() throws InterruptedException {
        // Given
        AccountId accountId1 = AccountId.of(123L);
        CustomerId customerId1 = CustomerId.generate();
        Email email1 = Email.of("test1@example.com");

        AccountId accountId2 = AccountId.of(124L);
        CustomerId customerId2 = CustomerId.generate();
        Email email2 = Email.of("test2@example.com");

        // When
        AccountCreatedEvent event1 = new AccountCreatedEvent(accountId1, customerId1, email1);
        Thread.sleep(1); // 시간 차이를 만들기 위한 대기
        AccountCreatedEvent event2 = new AccountCreatedEvent(accountId2, customerId2, email2);

        // Then
        assertThat(event2.getOccurredAt()).isAfterOrEqualTo(event1.getOccurredAt());
    }

    @Test
    @DisplayName("같은 정보로 생성된 이벤트라도 발생 시간이 다르면 다른 객체이다")
    void eventsWithSameDataButDifferentTimeAreNotEqual() throws InterruptedException {
        // Given
        AccountId accountId = AccountId.of(123L);
        CustomerId customerId = CustomerId.generate();
        Email email = Email.of("test@example.com");

        // When
        AccountCreatedEvent event1 = new AccountCreatedEvent(accountId, customerId, email);
        Thread.sleep(1);
        AccountCreatedEvent event2 = new AccountCreatedEvent(accountId, customerId, email);

        // Then
        assertThat(event1).isNotSameAs(event2);
        assertThat(event1.getOccurredAt()).isNotEqualTo(event2.getOccurredAt());
    }

    @Test
    @DisplayName("이벤트의 모든 필드가 올바르게 설정된다")
    void allFieldsAreSetCorrectly() {
        // Given
        AccountId accountId = AccountId.of(123L);
        CustomerId customerId = CustomerId.of(456L);
        Email email = Email.of("user@domain.com");

        // When
        AccountCreatedEvent event = new AccountCreatedEvent(accountId, customerId, email);

        // Then
        assertThat(event.getAccountId().getValue()).isEqualTo(123L);
        assertThat(event.getCustomerId().getValue()).isEqualTo(456L);
        assertThat(event.getEmail().getValue()).isEqualTo("user@domain.com");
        assertThat(event.getOccurredAt()).isNotNull();
    }
}