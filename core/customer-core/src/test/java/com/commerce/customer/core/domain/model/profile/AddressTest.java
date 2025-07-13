package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Address 엔티티 테스트")
class AddressTest {

    @Test
    @DisplayName("유효한 정보로 주소를 생성할 수 있다")
    void createAddressWithValidInfo() {
        // Given
        AddressType type = AddressType.HOME;
        String alias = "집";
        String zipCode = "12345";
        String roadAddress = "서울특별시 강남구 테헤란로 123";
        String detailAddress = "456호";

        // When
        Address address = Address.create(type, alias, zipCode, roadAddress, detailAddress);

        // Then
        assertThat(address).isNotNull();
        assertThat(address.getAddressId()).isNotNull();
        assertThat(address.getType()).isEqualTo(type);
        assertThat(address.getAlias()).isEqualTo(alias);
        assertThat(address.getZipCode()).isEqualTo(zipCode);
        assertThat(address.getRoadAddress()).isEqualTo(roadAddress);
        assertThat(address.getDetailAddress()).isEqualTo(detailAddress);
        assertThat(address.isDefault()).isFalse();
        assertThat(address.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("별칭을 업데이트할 수 있다")
    void updateAlias() {
        // Given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", "456호");
        String newAlias = "우리집";

        // When
        address.updateAlias(newAlias);

        // Then
        assertThat(address.getAlias()).isEqualTo(newAlias);
    }

    @Test
    @DisplayName("배송 메모를 업데이트할 수 있다")
    void updateDeliveryMemo() {
        // Given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", "456호");
        String memo = "문 앞에 놓아주세요";

        // When
        address.updateDeliveryMemo(memo);

        // Then
        assertThat(address.getDeliveryMemo()).isEqualTo(memo);
    }

    @Test
    @DisplayName("기본 주소로 설정할 수 있다")
    void setAsDefault() {
        // Given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", "456호");

        // When
        address.setAsDefault();

        // Then
        assertThat(address.isDefault()).isTrue();
    }

    @Test
    @DisplayName("기본 주소 설정을 해제할 수 있다")
    void removeDefault() {
        // Given
        Address address = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", "456호");
        address.setAsDefault();

        // When
        address.removeDefault();

        // Then
        assertThat(address.isDefault()).isFalse();
    }

    @Test
    @DisplayName("주소 타입이 null이면 예외가 발생한다")
    void throwExceptionWhenTypeIsNull() {
        // When & Then
        assertThatThrownBy(() -> Address.create(null, "집", "12345", "서울특별시 강남구 테헤란로 123", "456호"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("주소 타입은 필수값입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    @DisplayName("별칭이 null이거나 비어있으면 예외가 발생한다")
    void throwExceptionWhenAliasIsNullOrEmpty(String alias) {
        // When & Then
        assertThatThrownBy(() -> Address.create(AddressType.HOME, alias, "12345", "서울특별시 강남구 테헤란로 123", "456호"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주소 별칭은 필수값입니다.");
    }

    @Test
    @DisplayName("우편번호가 null이면 예외가 발생한다")
    void throwExceptionWhenZipCodeIsNull() {
        // When & Then
        assertThatThrownBy(() -> Address.create(AddressType.HOME, "집", null, "서울특별시 강남구 테헤란로 123", "456호"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("우편번호는 필수값입니다.");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   ", "1234", "123456"})
    @DisplayName("우편번호 형식이 잘못되면 예외가 발생한다")
    void throwExceptionWhenZipCodeFormatIsInvalid(String zipCode) {
        // When & Then
        assertThatThrownBy(() -> Address.create(AddressType.HOME, "집", zipCode, "서울특별시 강남구 테헤란로 123", "456호"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("우편번호는 5자리 숫자여야 합니다.");
    }

    @Test
    @DisplayName("도로명 주소가 null이면 예외가 발생한다")
    void throwExceptionWhenRoadAddressIsNull() {
        // When & Then
        assertThatThrownBy(() -> Address.create(AddressType.HOME, "집", "12345", null, "456호"))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("도로명 주소는 필수값입니다.");
    }


    @Test
    @DisplayName("별칭이 너무 길면 예외가 발생한다")
    void throwExceptionWhenAliasTooLong() {
        // Given
        String longAlias = "a".repeat(51);

        // When & Then
        assertThatThrownBy(() -> Address.create(AddressType.HOME, longAlias, "12345", "서울특별시 강남구 테헤란로 123", "456호"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주소 별칭은 50자를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("동일한 ID를 가진 Address는 같다고 판단된다")
    void equalityTest() {
        // Given
        Address address1 = Address.create(AddressType.HOME, "집", "12345", "서울특별시 강남구 테헤란로 123", "456호");
        Address address2 = Address.create(AddressType.WORK, "회사", "54321", "부산광역시 해운대구 센텀로 456", "789호");

        // When & Then
        assertThat(address1).isEqualTo(address1); // 자기 자신과는 같음
        assertThat(address1).isNotEqualTo(address2); // 다른 주소와는 다름
        assertThat(address1.hashCode()).isEqualTo(address1.hashCode());
    }
}