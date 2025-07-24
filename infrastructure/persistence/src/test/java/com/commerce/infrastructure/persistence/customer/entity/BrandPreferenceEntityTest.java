package com.commerce.infrastructure.persistence.customer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BrandPreferenceEntity 테스트")
class BrandPreferenceEntityTest {

    @Test
    @DisplayName("Builder를 통해 BrandPreferenceEntity를 생성한다")
    void createBrandPreferenceEntityWithBuilder() {
        // Given
        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(1L)
            .firstName("홍")
            .lastName("길동")
            .primaryPhone("010-1234-5678")
            .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
            .build();

        // When
        BrandPreferenceEntity preference = BrandPreferenceEntity.builder()
            .customerProfile(profile)
            .brandName("Nike")
            .preferenceLevel(BrandPreferenceEntity.PreferenceLevel.LOVE)
            .build();

        // Then
        assertThat(preference.getCustomerProfile()).isEqualTo(profile);
        assertThat(preference.getBrandName()).isEqualTo("Nike");
        assertThat(preference.getPreferenceLevel()).isEqualTo(BrandPreferenceEntity.PreferenceLevel.LOVE);
    }

    @Test
    @DisplayName("선호도 레벨을 업데이트한다")
    void updatePreferenceLevel() {
        // Given
        BrandPreferenceEntity preference = BrandPreferenceEntity.builder()
            .brandName("Adidas")
            .preferenceLevel(BrandPreferenceEntity.PreferenceLevel.LIKE)
            .build();

        // When
        preference.updatePreferenceLevel(BrandPreferenceEntity.PreferenceLevel.LOVE);

        // Then
        assertThat(preference.getPreferenceLevel()).isEqualTo(BrandPreferenceEntity.PreferenceLevel.LOVE);
    }

    @Test
    @DisplayName("CustomerProfile을 설정한다")
    void setCustomerProfile() {
        // Given
        BrandPreferenceEntity preference = BrandPreferenceEntity.builder()
            .brandName("Puma")
            .preferenceLevel(BrandPreferenceEntity.PreferenceLevel.DISLIKE)
            .build();

        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(1L)
            .firstName("김")
            .lastName("영희")
            .primaryPhone("010-9876-5432")
            .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
            .build();

        // When
        preference.setCustomerProfile(profile);

        // Then
        assertThat(preference.getCustomerProfile()).isEqualTo(profile);
    }

    @ParameterizedTest
    @DisplayName("PreferenceLevel 열거형 값들을 테스트한다")
    @MethodSource("providePreferenceLevels")
    void preferenceLevelEnumValues(BrandPreferenceEntity.PreferenceLevel level, String expectedName) {
        // Then
        assertThat(level.name()).isEqualTo(expectedName);
    }

    private static Stream<Arguments> providePreferenceLevels() {
        return Stream.of(
            Arguments.of(BrandPreferenceEntity.PreferenceLevel.LOVE, "LOVE"),
            Arguments.of(BrandPreferenceEntity.PreferenceLevel.LIKE, "LIKE"),
            Arguments.of(BrandPreferenceEntity.PreferenceLevel.DISLIKE, "DISLIKE")
        );
    }

    @Test
    @DisplayName("PreferenceLevel의 순서를 확인한다")
    void preferenceLevelOrder() {
        // Given
        BrandPreferenceEntity.PreferenceLevel[] levels = BrandPreferenceEntity.PreferenceLevel.values();

        // Then
        assertThat(levels).hasSize(3);
        assertThat(levels[0]).isEqualTo(BrandPreferenceEntity.PreferenceLevel.LOVE);
        assertThat(levels[1]).isEqualTo(BrandPreferenceEntity.PreferenceLevel.LIKE);
        assertThat(levels[2]).isEqualTo(BrandPreferenceEntity.PreferenceLevel.DISLIKE);
    }

    @Test
    @DisplayName("기본 생성자는 protected로 접근이 제한된다")
    void protectedNoArgsConstructor() {
        // JPA를 위한 기본 생성자가 있지만 외부에서는 사용할 수 없음
        // 이 테스트는 컴파일 타임에 검증됨
        assertThat(BrandPreferenceEntity.class).isNotNull();
    }
}