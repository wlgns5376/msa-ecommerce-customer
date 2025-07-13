package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AddressId 값객체 테스트")
class AddressIdTest {

    @Test
    @DisplayName("유효한 Long 값으로 AddressId를 생성할 수 있다")
    void createAddressIdWithValidLong() {
        // Given
        Long validId = 1L;

        // When
        AddressId addressId = AddressId.of(validId);

        // Then
        assertThat(addressId).isNotNull();
        assertThat(addressId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("자동 생성된 AddressId는 양수여야 한다")
    void generateAddressIdShouldBePositive() {
        // When
        AddressId addressId = AddressId.generate();

        // Then
        assertThat(addressId).isNotNull();
        assertThat(addressId.getValue()).isPositive();
    }

    @Test
    @DisplayName("연속으로 생성된 AddressId는 증가하는 값을 가진다")
    void consecutiveGeneratedIdsShouldIncrement() {
        // When
        AddressId id1 = AddressId.generate();
        AddressId id2 = AddressId.generate();

        // Then
        assertThat(id2.getValue()).isGreaterThan(id1.getValue());
    }

    @Test
    @DisplayName("null 값으로 AddressId 생성 시 예외가 발생한다")
    void throwExceptionWhenIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> AddressId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주소 ID는 양수여야 합니다.");
    }

    @Test
    @DisplayName("0 이하의 값으로 AddressId 생성 시 예외가 발생한다")
    void throwExceptionWhenIdIsZeroOrNegative() {
        // When & Then
        assertThatThrownBy(() -> AddressId.of(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주소 ID는 양수여야 합니다.");

        assertThatThrownBy(() -> AddressId.of(-1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주소 ID는 양수여야 합니다.");
    }

    @Test
    @DisplayName("동일한 값을 가진 AddressId는 같다고 판단된다")
    void equalityTest() {
        // Given
        Long id = 123L;
        AddressId addressId1 = AddressId.of(id);
        AddressId addressId2 = AddressId.of(id);

        // When & Then
        assertThat(addressId1).isEqualTo(addressId2);
        assertThat(addressId1.hashCode()).isEqualTo(addressId2.hashCode());
    }

    @Test
    @DisplayName("다른 값을 가진 AddressId는 다르다고 판단된다")
    void inequalityTest() {
        // Given
        AddressId addressId1 = AddressId.of(123L);
        AddressId addressId2 = AddressId.of(456L);

        // When & Then
        assertThat(addressId1).isNotEqualTo(addressId2);
    }

    @Test
    @DisplayName("toString은 Long 값의 문자열 표현을 반환한다")
    void toStringTest() {
        // Given
        Long id = 123L;
        AddressId addressId = AddressId.of(id);

        // When & Then
        assertThat(addressId.toString()).isEqualTo("123");
    }
}