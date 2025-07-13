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

@DisplayName("FullName 값 객체 테스트")
class FullNameTest {

    @Test
    @DisplayName("유효한 이름으로 FullName을 생성할 수 있다")
    void createFullNameWithValidNames() {
        // Given
        String firstName = "길동";
        String lastName = "홍";

        // When
        FullName fullName = FullName.of(firstName, lastName);

        // Then
        assertThat(fullName).isNotNull();
        assertThat(fullName.getFirstName()).isEqualTo(firstName);
        assertThat(fullName.getLastName()).isEqualTo(lastName);
    }

    @Test
    @DisplayName("전체 이름을 표시 형식으로 반환할 수 있다")
    void getDisplayName() {
        // Given
        FullName fullName = FullName.of("길동", "홍");

        // When
        String displayName = fullName.getDisplayName();

        // Then
        assertThat(displayName).isEqualTo("홍길동");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("firstName이 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenFirstNameIsNullOrEmpty(String firstName) {
        // When & Then
        assertThatThrownBy(() -> FullName.of(firstName, "홍"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이름은 필수값입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("lastName이 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenLastNameIsNullOrEmpty(String lastName) {
        // When & Then
        assertThatThrownBy(() -> FullName.of("길동", lastName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("성은 필수값입니다.");
    }

    @ParameterizedTest
    @MethodSource("provideInvalidNames")
    @DisplayName("이름이 너무 길면 예외가 발생한다")
    void throwExceptionWhenNameIsTooLong(String firstName, String lastName) {
        // When & Then
        assertThatThrownBy(() -> FullName.of(firstName, lastName))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("이름은 50자를 초과할 수 없습니다.");
    }

    private static Stream<Arguments> provideInvalidNames() {
        String longName = "a".repeat(51);
        return Stream.of(
            Arguments.of(longName, "홍"),
            Arguments.of("길동", longName)
        );
    }

    @Test
    @DisplayName("동일한 이름을 가진 FullName은 같다고 판단된다")
    void equalityTest() {
        // Given
        FullName fullName1 = FullName.of("길동", "홍");
        FullName fullName2 = FullName.of("길동", "홍");
        FullName fullName3 = FullName.of("철수", "김");

        // When & Then
        assertThat(fullName1).isEqualTo(fullName2);
        assertThat(fullName1).isNotEqualTo(fullName3);
        assertThat(fullName1.hashCode()).isEqualTo(fullName2.hashCode());
    }
}