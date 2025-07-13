package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BirthDate 값 객체 테스트")
class BirthDateTest {

    @Test
    @DisplayName("유효한 생년월일로 BirthDate를 생성할 수 있다")
    void createBirthDateWithValidDate() {
        // Given
        LocalDate date = LocalDate.of(1990, 5, 15);

        // When
        BirthDate birthDate = BirthDate.of(date);

        // Then
        assertThat(birthDate).isNotNull();
        assertThat(birthDate.getDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("나이를 계산할 수 있다")
    void calculateAge() {
        // Given
        LocalDate birthDate = LocalDate.now().minusYears(25).minusMonths(3);
        BirthDate birth = BirthDate.of(birthDate);

        // When
        int age = birth.getAge();

        // Then
        assertThat(age).isEqualTo(25);
    }

    @Test
    @DisplayName("나이대를 계산할 수 있다")
    void calculateAgeGroup() {
        // Given & When & Then
        assertThat(BirthDate.of(LocalDate.now().minusYears(15)).getAgeGroup()).isEqualTo("10대");
        assertThat(BirthDate.of(LocalDate.now().minusYears(25)).getAgeGroup()).isEqualTo("20대");
        assertThat(BirthDate.of(LocalDate.now().minusYears(35)).getAgeGroup()).isEqualTo("30대");
        assertThat(BirthDate.of(LocalDate.now().minusYears(45)).getAgeGroup()).isEqualTo("40대");
        assertThat(BirthDate.of(LocalDate.now().minusYears(55)).getAgeGroup()).isEqualTo("50대");
        assertThat(BirthDate.of(LocalDate.now().minusYears(65)).getAgeGroup()).isEqualTo("60대 이상");
    }

    @Test
    @DisplayName("null 생년월일로 생성하면 예외가 발생한다")
    void throwExceptionWhenDateIsNull() {
        // When & Then
        assertThatThrownBy(() -> BirthDate.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("생년월일은 필수값입니다.");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidDates")
    @DisplayName("잘못된 생년월일로 생성하면 예외가 발생한다")
    void throwExceptionWhenInvalidDate(LocalDate date, String expectedMessage) {
        // When & Then
        assertThatThrownBy(() -> BirthDate.of(date))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> provideInvalidDates() {
        LocalDate today = LocalDate.now();
        return Stream.of(
            Arguments.of(today.plusDays(1), "생년월일은 미래일 수 없습니다."),
            Arguments.of(today.minusYears(13), "만 14세 이상만 가입 가능합니다."),
            Arguments.of(today.minusYears(150), "올바르지 않은 생년월일입니다.")
        );
    }

    @ParameterizedTest
    @MethodSource("provideValidDates")
    @DisplayName("유효한 생년월일로 생성할 수 있다")
    void createWithValidDates(LocalDate date) {
        // When & Then
        assertThatCode(() -> BirthDate.of(date))
            .doesNotThrowAnyException();
    }

    private static Stream<LocalDate> provideValidDates() {
        LocalDate today = LocalDate.now();
        return Stream.of(
            today.minusYears(14),   // 최소 나이
            today.minusYears(25),   // 일반적인 나이
            today.minusYears(65),   // 고령
            today.minusYears(100)   // 최대 나이 근처
        );
    }

    @Test
    @DisplayName("동일한 생년월일을 가진 BirthDate는 같다고 판단된다")
    void equalityTest() {
        // Given
        LocalDate date = LocalDate.of(1990, 5, 15);
        BirthDate birthDate1 = BirthDate.of(date);
        BirthDate birthDate2 = BirthDate.of(date);
        BirthDate birthDate3 = BirthDate.of(LocalDate.of(1995, 3, 20));

        // When & Then
        assertThat(birthDate1).isEqualTo(birthDate2);
        assertThat(birthDate1).isNotEqualTo(birthDate3);
        assertThat(birthDate1.hashCode()).isEqualTo(birthDate2.hashCode());
    }
}