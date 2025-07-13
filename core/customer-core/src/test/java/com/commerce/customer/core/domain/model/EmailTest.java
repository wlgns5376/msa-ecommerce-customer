package com.commerce.customer.core.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email 값 객체 테스트")
class EmailTest {

    @Test
    @DisplayName("유효한 이메일 주소로 Email 객체를 생성할 수 있다")
    void createEmail_WithValidAddress_ShouldSuccess() {
        // Given
        String validEmail = "test@example.com";

        // When
        Email email = Email.of(validEmail);

        // Then
        assertThat(email.getValue()).isEqualTo(validEmail);
    }

    @Test
    @DisplayName("이메일 주소는 소문자로 정규화된다")
    void createEmail_WithUpperCase_ShouldNormalizeLowerCase() {
        // Given
        String upperCaseEmail = "TEST@EXAMPLE.COM";

        // When
        Email email = Email.of(upperCaseEmail);

        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("앞뒤 공백이 제거된다")
    void createEmail_WithWhitespace_ShouldTrimWhitespace() {
        // Given
        String emailWithSpaces = "  test@example.com  ";

        // When
        Email email = Email.of(emailWithSpaces);

        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@domain.co.kr",
        "test+tag@example.org",
        "test_123@example-domain.com",
        "test.email+tag+sorting@example.com"
    })
    @DisplayName("다양한 유효한 이메일 형식을 허용한다")
    void createEmail_WithVariousValidFormats_ShouldSuccess(String validEmail) {
        // When & Then
        assertThatNoException().isThrownBy(() -> Email.of(validEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        " ",
        "invalid-email",
        "@example.com",
        "test@",
        "test.example.com",
        "test@.com",
        "test@com",
        "test..test@example.com",
        "test@example.",
        "test@.example.com"
    })
    @DisplayName("잘못된 이메일 형식은 예외를 발생시킨다")
    void createEmail_WithInvalidFormats_ShouldThrowException(String invalidEmail) {
        // When & Then
        assertThatThrownBy(() -> Email.of(invalidEmail))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("null 이메일은 예외를 발생시킨다")
    void createEmail_WithNull_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> Email.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("이메일은 필수값입니다");
    }

    @Test
    @DisplayName("같은 이메일 주소는 동일하다고 판단된다")
    void equals_WithSameEmailAddress_ShouldReturnTrue() {
        // Given
        Email email1 = Email.of("test@example.com");
        Email email2 = Email.of("TEST@EXAMPLE.COM"); // 대소문자 다름

        // When & Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }

    @Test
    @DisplayName("다른 이메일 주소는 동일하지 않다고 판단된다")
    void equals_WithDifferentEmailAddress_ShouldReturnFalse() {
        // Given
        Email email1 = Email.of("test1@example.com");
        Email email2 = Email.of("test2@example.com");

        // When & Then
        assertThat(email1).isNotEqualTo(email2);
    }
}