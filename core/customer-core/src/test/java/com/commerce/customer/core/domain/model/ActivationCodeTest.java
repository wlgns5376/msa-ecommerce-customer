package com.commerce.customer.core.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class ActivationCodeTest {

    @Test
    void 인증코드_생성_테스트() {
        // when
        ActivationCode code = ActivationCode.generate();
        
        // then
        assertThat(code).isNotNull();
        assertThat(code.getCode()).hasSize(32);
        assertThat(code.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(code.isExpired()).isFalse();
    }

    @Test
    void 인증코드_매칭_테스트() {
        // given
        String codeValue = "12345678901234567890123456789012";
        ActivationCode code = ActivationCode.of(codeValue, LocalDateTime.now().plusHours(1));
        
        // when & then
        assertThat(code.matches(codeValue)).isTrue();
        assertThat(code.matches("wrong_code")).isFalse();
    }

    @Test
    void 만료된_인증코드_테스트() {
        // given
        ActivationCode code = ActivationCode.of("12345678901234567890123456789012", 
                                               LocalDateTime.now().minusHours(1));
        
        // when & then
        assertThat(code.isExpired()).isTrue();
    }

    @Test
    void 잘못된_인증코드_생성시_예외발생() {
        // when & then
        assertThatThrownBy(() -> ActivationCode.of("", LocalDateTime.now().plusHours(1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("인증 코드는 32자 이상이어야 합니다.");
            
        assertThatThrownBy(() -> ActivationCode.of("short", LocalDateTime.now().plusHours(1)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("인증 코드는 32자 이상이어야 합니다.");
    }
}