package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BrandPreference 값객체 테스트")
class BrandPreferenceTest {

    @Test
    @DisplayName("유효한 정보로 BrandPreference를 생성할 수 있다")
    void createBrandPreferenceWithValidInfo() {
        // Given
        String brandId = "BRAND001";
        String brandName = "나이키";
        PreferenceLevel level = PreferenceLevel.LOVE;

        // When
        BrandPreference preference = BrandPreference.of(brandId, brandName, level);

        // Then
        assertThat(preference).isNotNull();
        assertThat(preference.getBrandId()).isEqualTo(brandId);
        assertThat(preference.getBrandName()).isEqualTo(brandName);
        assertThat(preference.getLevel()).isEqualTo(level);
    }

    @Test
    @DisplayName("다양한 선호도 레벨로 BrandPreference를 생성할 수 있다")
    void createBrandPreferenceWithDifferentLevels() {
        // Given
        String brandId = "BRAND001";
        String brandName = "아디다스";

        // When & Then
        BrandPreference highPreference = BrandPreference.of(brandId, brandName, PreferenceLevel.LOVE);
        assertThat(highPreference.getLevel()).isEqualTo(PreferenceLevel.LOVE);

        BrandPreference mediumPreference = BrandPreference.of(brandId, brandName, PreferenceLevel.LIKE);
        assertThat(mediumPreference.getLevel()).isEqualTo(PreferenceLevel.LIKE);

        BrandPreference lowPreference = BrandPreference.of(brandId, brandName, PreferenceLevel.DISLIKE);
        assertThat(lowPreference.getLevel()).isEqualTo(PreferenceLevel.DISLIKE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("브랜드 ID가 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenBrandIdIsNullOrEmpty(String brandId) {
        // When & Then
        assertThatThrownBy(() -> BrandPreference.of(brandId, "나이키", PreferenceLevel.LOVE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("브랜드 ID는 필수값입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("브랜드 이름이 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenBrandNameIsNullOrEmpty(String brandName) {
        // When & Then
        assertThatThrownBy(() -> BrandPreference.of("BRAND001", brandName, PreferenceLevel.LOVE))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("브랜드 이름은 필수값입니다.");
    }

    @Test
    @DisplayName("선호도 레벨이 null이면 예외가 발생한다")
    void throwExceptionWhenLevelIsNull() {
        // When & Then
        assertThatThrownBy(() -> BrandPreference.of("BRAND001", "나이키", null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("선호도 레벨은 필수값입니다.");
    }

    @Test
    @DisplayName("동일한 브랜드 ID를 가진 BrandPreference는 같다고 판단된다")
    void equalityBasedOnBrandId() {
        // Given
        String brandId = "BRAND001";
        BrandPreference preference1 = BrandPreference.of(brandId, "나이키", PreferenceLevel.LOVE);
        BrandPreference preference2 = BrandPreference.of(brandId, "Nike", PreferenceLevel.LIKE); // 다른 이름과 레벨

        // When & Then
        assertThat(preference1).isEqualTo(preference2);
        assertThat(preference1.hashCode()).isEqualTo(preference2.hashCode());
    }

    @Test
    @DisplayName("다른 브랜드 ID를 가진 BrandPreference는 다르다고 판단된다")
    void inequalityBasedOnBrandId() {
        // Given
        BrandPreference preference1 = BrandPreference.of("BRAND001", "나이키", PreferenceLevel.LOVE);
        BrandPreference preference2 = BrandPreference.of("BRAND002", "아디다스", PreferenceLevel.LOVE);

        // When & Then
        assertThat(preference1).isNotEqualTo(preference2);
    }

    @Test
    @DisplayName("자기 자신과는 같다고 판단된다")
    void equalityWithSelf() {
        // Given
        BrandPreference preference = BrandPreference.of("BRAND001", "나이키", PreferenceLevel.LOVE);

        // When & Then
        assertThat(preference).isEqualTo(preference);
    }

    @Test
    @DisplayName("null과는 다르다고 판단된다")
    void inequalityWithNull() {
        // Given
        BrandPreference preference = BrandPreference.of("BRAND001", "나이키", PreferenceLevel.LOVE);

        // When & Then
        assertThat(preference).isNotEqualTo(null);
    }

    @Test
    @DisplayName("다른 클래스 객체와는 다르다고 판단된다")
    void inequalityWithDifferentClass() {
        // Given
        BrandPreference preference = BrandPreference.of("BRAND001", "나이키", PreferenceLevel.LOVE);
        String otherObject = "다른 객체";

        // When & Then
        assertThat(preference).isNotEqualTo(otherObject);
    }

    @Test
    @DisplayName("toString 메서드가 올바르게 동작한다")
    void toStringTest() {
        // Given
        BrandPreference preference = BrandPreference.of("BRAND001", "나이키", PreferenceLevel.LOVE);

        // When
        String result = preference.toString();

        // Then
        assertThat(result).contains("BrandPreference");
        assertThat(result).contains("brandId='BRAND001'");
        assertThat(result).contains("brandName='나이키'");
        assertThat(result).contains("level=LOVE");
    }

    @Test
    @DisplayName("브랜드 ID에 공백이 포함된 경우 트림되지 않고 유효성 검사를 통과한다")
    void acceptBrandIdWithSpaces() {
        // Given
        String brandIdWithSpaces = "BRAND 001";
        String brandName = "나이키";
        PreferenceLevel level = PreferenceLevel.LOVE;

        // When
        BrandPreference preference = BrandPreference.of(brandIdWithSpaces, brandName, level);

        // Then
        assertThat(preference.getBrandId()).isEqualTo(brandIdWithSpaces);
    }

    @Test
    @DisplayName("브랜드 이름에 공백이 포함된 경우 트림되지 않고 유효성 검사를 통과한다")
    void acceptBrandNameWithSpaces() {
        // Given
        String brandId = "BRAND001";
        String brandNameWithSpaces = "나이키 코리아";
        PreferenceLevel level = PreferenceLevel.LOVE;

        // When
        BrandPreference preference = BrandPreference.of(brandId, brandNameWithSpaces, level);

        // Then
        assertThat(preference.getBrandName()).isEqualTo(brandNameWithSpaces);
    }
}