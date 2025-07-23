package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginSuccessfulEvent 테스트")
class LoginSuccessfulEventTest {

    @Test
    @DisplayName("로그인 성공 이벤트 생성")
    void createLoginSuccessfulEvent() {
        // given
        AccountId accountId = AccountId.of(1L);
        CustomerId customerId = CustomerId.of(12345L);
        LocalDateTime loginAt = LocalDateTime.now();
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        LoginSuccessfulEvent event = new LoginSuccessfulEvent(accountId, customerId, loginAt);

        // then
        assertThat(event.getAccountId()).isEqualTo(accountId);
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getLoginAt()).isEqualTo(loginAt);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("로그인 시간과 이벤트 발생 시간의 차이 확인")
    void verifyLoginTimeAndOccurredTimeDifference() {
        // given
        AccountId accountId = AccountId.of(1L);
        CustomerId customerId = CustomerId.of(12345L);
        LocalDateTime loginAt = LocalDateTime.now().minusMinutes(5); // 5분 전 로그인

        // when
        LoginSuccessfulEvent event = new LoginSuccessfulEvent(accountId, customerId, loginAt);

        // then
        assertThat(event.getLoginAt()).isBefore(event.getOccurredAt());
        assertThat(event.getOccurredAt()).isAfter(loginAt);
    }
}