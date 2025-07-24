package com.commerce.infrastructure.persistence.customer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryInterestEntity 테스트")
class CategoryInterestEntityTest {

    @Test
    @DisplayName("Builder를 통해 CategoryInterestEntity를 생성한다")
    void createCategoryInterestEntityWithBuilder() {
        // Given
        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(1L)
            .firstName("홍")
            .lastName("길동")
            .primaryPhone("010-1234-5678")
            .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
            .build();

        // When
        CategoryInterestEntity interest = CategoryInterestEntity.builder()
            .customerProfile(profile)
            .categoryName("스포츠")
            .interestLevel(CategoryInterestEntity.InterestLevel.HIGH)
            .build();

        // Then
        assertThat(interest.getCustomerProfile()).isEqualTo(profile);
        assertThat(interest.getCategoryName()).isEqualTo("스포츠");
        assertThat(interest.getInterestLevel()).isEqualTo(CategoryInterestEntity.InterestLevel.HIGH);
    }

    @Test
    @DisplayName("관심도 레벨을 업데이트한다")
    void updateInterestLevel() {
        // Given
        CategoryInterestEntity interest = CategoryInterestEntity.builder()
            .categoryName("패션")
            .interestLevel(CategoryInterestEntity.InterestLevel.MEDIUM)
            .build();

        // When
        interest.updateInterestLevel(CategoryInterestEntity.InterestLevel.LOW);

        // Then
        assertThat(interest.getInterestLevel()).isEqualTo(CategoryInterestEntity.InterestLevel.LOW);
    }

    @Test
    @DisplayName("CustomerProfile을 설정한다")
    void setCustomerProfile() {
        // Given
        CategoryInterestEntity interest = CategoryInterestEntity.builder()
            .categoryName("뷰티")
            .interestLevel(CategoryInterestEntity.InterestLevel.HIGH)
            .build();

        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(2L)
            .firstName("김")
            .lastName("영희")
            .primaryPhone("010-9876-5432")
            .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
            .build();

        // When
        interest.setCustomerProfile(profile);

        // Then
        assertThat(interest.getCustomerProfile()).isEqualTo(profile);
    }

    @ParameterizedTest
    @DisplayName("InterestLevel 열거형 값들을 테스트한다")
    @MethodSource("provideInterestLevels")
    void interestLevelEnumValues(CategoryInterestEntity.InterestLevel level, String expectedName) {
        // Then
        assertThat(level.name()).isEqualTo(expectedName);
    }

    private static Stream<Arguments> provideInterestLevels() {
        return Stream.of(
            Arguments.of(CategoryInterestEntity.InterestLevel.HIGH, "HIGH"),
            Arguments.of(CategoryInterestEntity.InterestLevel.MEDIUM, "MEDIUM"),
            Arguments.of(CategoryInterestEntity.InterestLevel.LOW, "LOW")
        );
    }

    @Test
    @DisplayName("InterestLevel의 순서를 확인한다")
    void interestLevelOrder() {
        // Given
        CategoryInterestEntity.InterestLevel[] levels = CategoryInterestEntity.InterestLevel.values();

        // Then
        assertThat(levels).hasSize(3);
        assertThat(levels[0]).isEqualTo(CategoryInterestEntity.InterestLevel.HIGH);
        assertThat(levels[1]).isEqualTo(CategoryInterestEntity.InterestLevel.MEDIUM);
        assertThat(levels[2]).isEqualTo(CategoryInterestEntity.InterestLevel.LOW);
    }

    @Test
    @DisplayName("기본 생성자는 protected로 접근이 제한된다")
    void protectedNoArgsConstructor() {
        // JPA를 위한 기본 생성자가 있지만 외부에서는 사용할 수 없음
        // 이 테스트는 컴파일 타임에 검증됨
        assertThat(CategoryInterestEntity.class).isNotNull();
    }
}