package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProfileId 값객체 테스트")
class ProfileIdTest {

    @Test
    @DisplayName("유효한 Long 값으로 ProfileId를 생성할 수 있다")
    void createProfileIdWithValidLong() {
        // Given
        Long validId = 1L;

        // When
        ProfileId profileId = ProfileId.of(validId);

        // Then
        assertThat(profileId).isNotNull();
        assertThat(profileId.getValue()).isEqualTo(validId);
    }

    @Test
    @DisplayName("자동 생성된 ProfileId는 양수여야 한다")
    void generateProfileIdShouldBePositive() {
        // When
        ProfileId profileId = ProfileId.generate();

        // Then
        assertThat(profileId).isNotNull();
        assertThat(profileId.getValue()).isPositive();
    }

    @Test
    @DisplayName("연속으로 생성된 ProfileId는 증가하는 값을 가진다")
    void consecutiveGeneratedIdsShouldIncrement() {
        // When
        ProfileId id1 = ProfileId.generate();
        ProfileId id2 = ProfileId.generate();

        // Then
        assertThat(id2.getValue()).isGreaterThan(id1.getValue());
    }

    @Test
    @DisplayName("null 값으로 ProfileId 생성 시 예외가 발생한다")
    void throwExceptionWhenIdIsNull() {
        // When & Then
        assertThatThrownBy(() -> ProfileId.of(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("프로필 ID는 양수여야 합니다.");
    }

    @Test
    @DisplayName("0 이하의 값으로 ProfileId 생성 시 예외가 발생한다")
    void throwExceptionWhenIdIsZeroOrNegative() {
        // When & Then
        assertThatThrownBy(() -> ProfileId.of(0L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("프로필 ID는 양수여야 합니다.");

        assertThatThrownBy(() -> ProfileId.of(-1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("프로필 ID는 양수여야 합니다.");
    }

    @Test
    @DisplayName("동일한 값을 가진 ProfileId는 같다고 판단된다")
    void equalityTest() {
        // Given
        Long id = 123L;
        ProfileId profileId1 = ProfileId.of(id);
        ProfileId profileId2 = ProfileId.of(id);

        // When & Then
        assertThat(profileId1).isEqualTo(profileId2);
        assertThat(profileId1.hashCode()).isEqualTo(profileId2.hashCode());
    }

    @Test
    @DisplayName("다른 값을 가진 ProfileId는 다르다고 판단된다")
    void inequalityTest() {
        // Given
        ProfileId profileId1 = ProfileId.of(123L);
        ProfileId profileId2 = ProfileId.of(456L);

        // When & Then
        assertThat(profileId1).isNotEqualTo(profileId2);
    }

    @Test
    @DisplayName("toString은 Long 값의 문자열 표현을 반환한다")
    void toStringTest() {
        // Given
        Long id = 123L;
        ProfileId profileId = ProfileId.of(id);

        // When & Then
        assertThat(profileId.toString()).isEqualTo("123");
    }
}