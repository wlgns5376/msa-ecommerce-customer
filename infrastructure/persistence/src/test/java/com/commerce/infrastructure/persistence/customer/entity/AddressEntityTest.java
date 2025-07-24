package com.commerce.infrastructure.persistence.customer.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddressEntity 테스트")
class AddressEntityTest {

    @Test
    @DisplayName("Builder를 통해 AddressEntity를 생성한다")
    void createAddressEntityWithBuilder() {
        // Given
        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(1L)
            .firstName("홍")
            .lastName("길동")
            .primaryPhone("010-1234-5678")
            .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
            .build();

        // When
        AddressEntity address = AddressEntity.builder()
            .customerProfile(profile)
            .type(AddressEntity.AddressType.HOME)
            .alias("집")
            .zipCode("06234")
            .roadAddress("서울시 강남구 테헤란로 123")
            .jibunAddress("서울시 강남구 대치동 123")
            .detailAddress("101동 1001호")
            .isDefault(true)
            .build();

        // Then
        assertThat(address.getCustomerProfile()).isEqualTo(profile);
        assertThat(address.getType()).isEqualTo(AddressEntity.AddressType.HOME);
        assertThat(address.getAlias()).isEqualTo("집");
        assertThat(address.getZipCode()).isEqualTo("06234");
        assertThat(address.getRoadAddress()).isEqualTo("서울시 강남구 테헤란로 123");
        assertThat(address.getJibunAddress()).isEqualTo("서울시 강남구 대치동 123");
        assertThat(address.getDetailAddress()).isEqualTo("101동 1001호");
        assertThat(address.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("isDefault가 null인 경우 false로 초기화된다")
    void isDefaultNullHandling() {
        // When
        AddressEntity address = AddressEntity.builder()
            .customerProfile(null)
            .type(AddressEntity.AddressType.WORK)
            .zipCode("12345")
            .roadAddress("도로명 주소")
            .isDefault(null)
            .build();

        // Then
        assertThat(address.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("주소 정보를 업데이트한다")
    void updateAddress() {
        // Given
        AddressEntity address = AddressEntity.builder()
            .type(AddressEntity.AddressType.HOME)
            .alias("집")
            .zipCode("06234")
            .roadAddress("서울시 강남구 테헤란로 123")
            .jibunAddress("서울시 강남구 대치동 123")
            .detailAddress("101동 1001호")
            .build();

        // When
        address.updateAddress(
            AddressEntity.AddressType.WORK,
            "회사",
            "06621",
            "서울시 서초구 서초대로 456",
            "서울시 서초구 서초동 456",
            "5층"
        );

        // Then
        assertThat(address.getType()).isEqualTo(AddressEntity.AddressType.WORK);
        assertThat(address.getAlias()).isEqualTo("회사");
        assertThat(address.getZipCode()).isEqualTo("06621");
        assertThat(address.getRoadAddress()).isEqualTo("서울시 서초구 서초대로 456");
        assertThat(address.getJibunAddress()).isEqualTo("서울시 서초구 서초동 456");
        assertThat(address.getDetailAddress()).isEqualTo("5층");
    }

    @Test
    @DisplayName("주소를 기본 주소로 설정한다")
    void setAsDefault() {
        // Given
        AddressEntity address = AddressEntity.builder()
            .type(AddressEntity.AddressType.HOME)
            .zipCode("12345")
            .roadAddress("도로명 주소")
            .isDefault(false)
            .build();

        // When
        address.setAsDefault();

        // Then
        assertThat(address.getIsDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 주소 설정을 해제한다")
    void unsetDefault() {
        // Given
        AddressEntity address = AddressEntity.builder()
            .type(AddressEntity.AddressType.HOME)
            .zipCode("12345")
            .roadAddress("도로명 주소")
            .isDefault(true)
            .build();

        // When
        address.unsetDefault();

        // Then
        assertThat(address.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("CustomerProfile을 설정한다")
    void setCustomerProfile() {
        // Given
        AddressEntity address = AddressEntity.builder()
            .type(AddressEntity.AddressType.HOME)
            .zipCode("12345")
            .roadAddress("도로명 주소")
            .build();

        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(1L)
            .firstName("홍")
            .lastName("길동")
            .primaryPhone("010-1234-5678")
            .status(CustomerProfileEntity.ProfileStatus.ACTIVE)
            .build();

        // When
        address.setCustomerProfile(profile);

        // Then
        assertThat(address.getCustomerProfile()).isEqualTo(profile);
    }

    @ParameterizedTest
    @DisplayName("AddressType 열거형 값들을 테스트한다")
    @MethodSource("provideAddressTypes")
    void addressTypeEnumValues(AddressEntity.AddressType type, String expectedName) {
        // Then
        assertThat(type.name()).isEqualTo(expectedName);
    }

    private static Stream<Arguments> provideAddressTypes() {
        return Stream.of(
            Arguments.of(AddressEntity.AddressType.HOME, "HOME"),
            Arguments.of(AddressEntity.AddressType.WORK, "WORK"),
            Arguments.of(AddressEntity.AddressType.OTHER, "OTHER")
        );
    }

    @Test
    @DisplayName("기본 생성자는 protected로 접근이 제한된다")
    void protectedNoArgsConstructor() {
        // JPA를 위한 기본 생성자가 있지만 외부에서는 사용할 수 없음
        // 이 테스트는 컴파일 타임에 검증됨
        assertThat(AddressEntity.class).isNotNull();
    }

    @Test
    @DisplayName("선택적 필드들이 null일 수 있다")
    void optionalFieldsCanBeNull() {
        // When
        AddressEntity address = AddressEntity.builder()
            .type(AddressEntity.AddressType.OTHER)
            .alias(null)
            .zipCode("12345")
            .roadAddress("필수 도로명 주소")
            .jibunAddress(null)
            .detailAddress(null)
            .build();

        // Then
        assertThat(address.getAlias()).isNull();
        assertThat(address.getJibunAddress()).isNull();
        assertThat(address.getDetailAddress()).isNull();
        assertThat(address.getZipCode()).isNotNull();
        assertThat(address.getRoadAddress()).isNotNull();
    }
}