package com.commerce.customer.core.domain.event;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.jwt.JwtTokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenInvalidatedEvent 테스트")
class TokenInvalidatedEventTest {

    @Test
    @DisplayName("토큰 무효화 이벤트 생성")
    void createTokenInvalidatedEvent() {
        // given
        CustomerId customerId = CustomerId.of(12345L);
        JwtTokenType tokenType = JwtTokenType.ACCESS;
        String reason = "User logged out";
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        TokenInvalidatedEvent event = new TokenInvalidatedEvent(customerId, tokenType, reason);

        // then
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getTokenType()).isEqualTo(tokenType);
        assertThat(event.getReason()).isEqualTo(reason);
        assertThat(event.getOccurredAt()).isNotNull();
        assertThat(event.getOccurredAt()).isAfterOrEqualTo(beforeCreation);
        assertThat(event.getOccurredAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @ParameterizedTest
    @MethodSource("provideTokenInvalidationScenarios")
    @DisplayName("다양한 무효화 시나리오에 대한 이벤트 생성")
    void createTokenInvalidatedEvent_VariousScenarios(JwtTokenType tokenType, String reason) {
        // given
        CustomerId customerId = CustomerId.of(12345L);

        // when
        TokenInvalidatedEvent event = new TokenInvalidatedEvent(customerId, tokenType, reason);

        // then
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getTokenType()).isEqualTo(tokenType);
        assertThat(event.getReason()).isEqualTo(reason);
        assertThat(event.getOccurredAt()).isNotNull();
    }

    private static Stream<Arguments> provideTokenInvalidationScenarios() {
        return Stream.of(
            Arguments.of(JwtTokenType.ACCESS, "User logged out"),
            Arguments.of(JwtTokenType.ACCESS, "Token expired"),
            Arguments.of(JwtTokenType.ACCESS, "Security breach detected"),
            Arguments.of(JwtTokenType.REFRESH, "User logged out"),
            Arguments.of(JwtTokenType.REFRESH, "Token revoked"),
            Arguments.of(JwtTokenType.REFRESH, "User account deactivated")
        );
    }

    @Test
    @DisplayName("토큰 무효화 이벤트 - null reason")
    void createTokenInvalidatedEvent_NullReason() {
        // given
        CustomerId customerId = CustomerId.of(12345L);
        JwtTokenType tokenType = JwtTokenType.ACCESS;
        String reason = null;

        // when
        TokenInvalidatedEvent event = new TokenInvalidatedEvent(customerId, tokenType, reason);

        // then
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getTokenType()).isEqualTo(tokenType);
        assertThat(event.getReason()).isNull();
        assertThat(event.getOccurredAt()).isNotNull();
    }

    @Test
    @DisplayName("토큰 무효화 이벤트 - empty reason")
    void createTokenInvalidatedEvent_EmptyReason() {
        // given
        CustomerId customerId = CustomerId.of(12345L);
        JwtTokenType tokenType = JwtTokenType.REFRESH;
        String reason = "";

        // when
        TokenInvalidatedEvent event = new TokenInvalidatedEvent(customerId, tokenType, reason);

        // then
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getTokenType()).isEqualTo(tokenType);
        assertThat(event.getReason()).isEmpty();
        assertThat(event.getOccurredAt()).isNotNull();
    }
}