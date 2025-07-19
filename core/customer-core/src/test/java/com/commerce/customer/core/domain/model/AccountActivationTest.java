package com.commerce.customer.core.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class AccountActivationTest {

    private Account account;
    private final String validActivationCode = "12345678901234567890123456789012";

    @BeforeEach
    void setUp() {
        account = Account.create(
            CustomerId.of(1L),
            Email.of("test@example.com"),
            Password.of("Password123!")
        );
    }

    @Test
    void 계정_생성시_인증코드가_생성됨() {
        // then
        assertThat(account.getActivationCode()).isNotNull();
        assertThat(account.getActivationCode().getCode()).hasSize(32);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.PENDING);
    }

    @Test
    void 유효한_인증코드로_계정_활성화_성공() {
        // given
        String activationCode = account.getActivationCode().getCode();
        
        // when
        account.activate(activationCode);
        
        // then
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getActivationCode()).isNull();
        assertThat(account.getDomainEvents()).hasSize(1);
        assertThat(account.getDomainEvents().get(0)).isInstanceOf(AccountActivatedEvent.class);
    }

    @Test
    void 잘못된_인증코드로_활성화시_예외발생() {
        // when & then
        assertThatThrownBy(() -> account.activate("wrong_activation_code"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("잘못된 인증 코드입니다.");
    }

    @Test
    void 만료된_인증코드로_활성화시_예외발생() {
        // given
        Account expiredAccount = Account.restore(
            AccountId.of(1L),
            CustomerId.of(1L),
            Email.of("test@example.com"),
            Password.ofEncoded("encoded_password"),
            AccountStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            ActivationCode.of(validActivationCode, LocalDateTime.now().minusHours(1))
        );
        
        // when & then
        assertThatThrownBy(() -> expiredAccount.activate(validActivationCode))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("인증 코드가 만료되었습니다.");
    }

    @Test
    void 인증코드가_없는_계정_활성화시_예외발생() {
        // given
        Account accountWithoutCode = Account.restore(
            AccountId.of(1L),
            CustomerId.of(1L),
            Email.of("test@example.com"),
            Password.ofEncoded("encoded_password"),
            AccountStatus.PENDING,
            LocalDateTime.now(),
            LocalDateTime.now(),
            null,
            null
        );
        
        // when & then
        assertThatThrownBy(() -> accountWithoutCode.activate("any_code"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("인증 코드가 생성되지 않았습니다.");
    }

    @Test
    void 이미_활성화된_계정_재활성화시_예외발생() {
        // given
        String activationCode = account.getActivationCode().getCode();
        account.activate(activationCode);
        
        // when & then
        assertThatThrownBy(() -> account.activate("any_code"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("현재 상태에서는 활성화할 수 없습니다: ACTIVE");
    }

    @Test
    void 계정생성_이벤트_발생_확인() {
        // when
        account.raiseAccountCreatedEvent();
        
        // then
        assertThat(account.getDomainEvents()).hasSize(1);
        AccountCreatedEvent event = (AccountCreatedEvent) account.getDomainEvents().get(0);
        assertThat(event.getAccountId()).isEqualTo(account.getAccountId());
        assertThat(event.getCustomerId()).isEqualTo(account.getCustomerId());
        assertThat(event.getEmail()).isEqualTo(account.getEmail());
        assertThat(event.getActivationCode()).isEqualTo(account.getActivationCode().getCode());
    }
}