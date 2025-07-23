package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenGeneratedEvent 테스트")
class TokenGeneratedEventTest {

    @ParameterizedTest
    @EnumSource(JwtTokenType.class)
    @DisplayName("모든 토큰 타입에 대한 이벤트 생성")
    void createTokenGeneratedEvent_AllTokenTypes(JwtTokenType tokenType) {
        // given
        CustomerId customerId = CustomerId.of(12345L);
        AccountId accountId = AccountId.of(1L);
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        TokenGeneratedEvent event = new TokenGeneratedEvent(customerId, accountId, tokenType);

        // then
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getAccountId()).isEqualTo(accountId);
        assertThat(event.getTokenType()).isEqualTo(tokenType);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}