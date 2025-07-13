package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PhoneNumber 값 객체 테스트")
class PhoneNumberTest {

    @Test
    @DisplayName("유효한 한국 전화번호로 PhoneNumber를 생성할 수 있다")
    void createPhoneNumberWithValidKoreanNumber() {
        // Given
        String countryCode = "+82";
        String number = "10-1234-5678";

        // When
        PhoneNumber phoneNumber = PhoneNumber.of(countryCode, number);

        // Then
        assertThat(phoneNumber).isNotNull();
        assertThat(phoneNumber.getCountryCode()).isEqualTo(countryCode);
        assertThat(phoneNumber.getNumber()).isEqualTo(number);
    }

    @Test
    @DisplayName("한국 전화번호 간편 생성자를 사용할 수 있다")
    void createKoreanPhoneNumberWithSimpleConstructor() {
        // Given
        String number = "010-1234-5678";

        // When
        PhoneNumber phoneNumber = PhoneNumber.ofKorean(number);

        // Then
        assertThat(phoneNumber.getCountryCode()).isEqualTo("+82");
        assertThat(phoneNumber.getNumber()).isEqualTo(number);
    }

    @Test
    @DisplayName("포맷된 전화번호를 반환할 수 있다")
    void getFormattedNumber() {
        // Given
        PhoneNumber phoneNumber = PhoneNumber.of("+82", "010-1234-5678");

        // When
        String formatted = phoneNumber.getFormattedNumber();

        // Then
        assertThat(formatted).isEqualTo("+82 010-1234-5678");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("국가코드가 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenCountryCodeIsNullOrEmpty(String countryCode) {
        // When & Then
        assertThatThrownBy(() -> PhoneNumber.of(countryCode, "010-1234-5678"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("국가코드는 필수값입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("전화번호가 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenNumberIsNullOrEmpty(String number) {
        // When & Then
        assertThatThrownBy(() -> PhoneNumber.of("+82", number))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("전화번호는 필수값입니다.");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidKoreanNumbers")
    @DisplayName("잘못된 형식의 한국 전화번호는 예외가 발생한다")
    void throwExceptionWhenInvalidKoreanNumberFormat(String number) {
        // When & Then
        assertThatThrownBy(() -> PhoneNumber.ofKorean(number))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("올바르지 않은 한국 전화번호 형식입니다.");
    }

    private static Stream<String> provideInvalidKoreanNumbers() {
        return Stream.of(
            "010-123-4567",      // 자릿수 부족
            "010-12345-6789",    // 자릿수 초과
            "011-1234-5678",     // 잘못된 시작번호 (구형)
            "010-0000-0000",     // 모든 자릿수가 0
            "010-1234-567a",     // 숫자가 아닌 문자 포함
            "0101234567",        // 하이픈 없음
            "010_1234_5678"      // 잘못된 구분자
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidKoreanNumbers")
    @DisplayName("유효한 한국 전화번호 형식을 검증한다")
    void validateKoreanNumberFormat(String number) {
        // When & Then
        assertThatCode(() -> PhoneNumber.ofKorean(number))
            .doesNotThrowAnyException();
    }

    private static Stream<String> provideValidKoreanNumbers() {
        return Stream.of(
            "010-1234-5678",
            "010-9999-9999",
            "010-1000-0001"
        );
    }

    @Test
    @DisplayName("동일한 전화번호를 가진 PhoneNumber는 같다고 판단된다")
    void equalityTest() {
        // Given
        PhoneNumber phoneNumber1 = PhoneNumber.of("+82", "010-1234-5678");
        PhoneNumber phoneNumber2 = PhoneNumber.of("+82", "010-1234-5678");
        PhoneNumber phoneNumber3 = PhoneNumber.of("+82", "010-9999-9999");

        // When & Then
        assertThat(phoneNumber1).isEqualTo(phoneNumber2);
        assertThat(phoneNumber1).isNotEqualTo(phoneNumber3);
        assertThat(phoneNumber1.hashCode()).isEqualTo(phoneNumber2.hashCode());
    }
}