package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ContactInfo 값객체 테스트")
class ContactInfoTest {

    @Test
    @DisplayName("주 연락처만으로 ContactInfo를 생성할 수 있다")
    void createContactInfoWithPrimaryPhoneOnly() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");

        // When
        ContactInfo contactInfo = ContactInfo.of(primaryPhone);

        // Then
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getPrimaryPhone()).isEqualTo(primaryPhone);
        assertThat(contactInfo.getSecondaryPhone()).isNull();
    }

    @Test
    @DisplayName("주 연락처와 보조 연락처로 ContactInfo를 생성할 수 있다")
    void createContactInfoWithBothPhones() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber secondaryPhone = PhoneNumber.of("+82", "02-9876-5432");

        // When
        ContactInfo contactInfo = ContactInfo.of(primaryPhone, secondaryPhone);

        // Then
        assertThat(contactInfo).isNotNull();
        assertThat(contactInfo.getPrimaryPhone()).isEqualTo(primaryPhone);
        assertThat(contactInfo.getSecondaryPhone()).isEqualTo(secondaryPhone);
    }

    @Test
    @DisplayName("주 연락처를 업데이트할 수 있다")
    void updatePrimaryPhone() {
        // Given
        PhoneNumber originalPrimary = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber secondaryPhone = PhoneNumber.of("+82", "02-9876-5432");
        PhoneNumber newPrimary = PhoneNumber.ofKorean("010-9999-8888");
        
        ContactInfo contactInfo = ContactInfo.of(originalPrimary, secondaryPhone);

        // When
        ContactInfo updatedInfo = contactInfo.updatePrimaryPhone(newPrimary);

        // Then
        assertThat(updatedInfo.getPrimaryPhone()).isEqualTo(newPrimary);
        assertThat(updatedInfo.getSecondaryPhone()).isEqualTo(secondaryPhone);
    }

    @Test
    @DisplayName("보조 연락처를 업데이트할 수 있다")
    void updateSecondaryPhone() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber originalSecondary = PhoneNumber.of("+82", "02-9876-5432");
        PhoneNumber newSecondary = PhoneNumber.of("+82", "031-1111-2222");
        
        ContactInfo contactInfo = ContactInfo.of(primaryPhone, originalSecondary);

        // When
        ContactInfo updatedInfo = contactInfo.updateSecondaryPhone(newSecondary);

        // Then
        assertThat(updatedInfo.getPrimaryPhone()).isEqualTo(primaryPhone);
        assertThat(updatedInfo.getSecondaryPhone()).isEqualTo(newSecondary);
    }

    @Test
    @DisplayName("보조 연락처를 null로 설정할 수 있다")
    void updateSecondaryPhoneToNull() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber secondaryPhone = PhoneNumber.of("+82", "02-9876-5432");
        
        ContactInfo contactInfo = ContactInfo.of(primaryPhone, secondaryPhone);

        // When
        ContactInfo updatedInfo = contactInfo.updateSecondaryPhone(null);

        // Then
        assertThat(updatedInfo.getPrimaryPhone()).isEqualTo(primaryPhone);
        assertThat(updatedInfo.getSecondaryPhone()).isNull();
    }

    @Test
    @DisplayName("주 연락처가 null이면 예외가 발생한다")
    void throwExceptionWhenPrimaryPhoneIsNull() {
        // When & Then
        assertThatThrownBy(() -> ContactInfo.of(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("주 연락처는 필수값입니다.");

        assertThatThrownBy(() -> ContactInfo.of(null, PhoneNumber.of("+82", "02-1234-5678")))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("주 연락처는 필수값입니다.");
    }

    @Test
    @DisplayName("업데이트 시 주 연락처가 null이면 예외가 발생한다")
    void throwExceptionWhenUpdatingPrimaryPhoneToNull() {
        // Given
        ContactInfo contactInfo = ContactInfo.of(PhoneNumber.ofKorean("010-1234-5678"));

        // When & Then
        assertThatThrownBy(() -> contactInfo.updatePrimaryPhone(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("주 연락처는 필수값입니다.");
    }

    @Test
    @DisplayName("동일한 연락처 정보를 가진 ContactInfo는 같다고 판단된다")
    void equalityTest() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber secondaryPhone = PhoneNumber.of("+82", "02-9876-5432");

        ContactInfo info1 = ContactInfo.of(primaryPhone, secondaryPhone);
        ContactInfo info2 = ContactInfo.of(primaryPhone, secondaryPhone);

        // When & Then
        assertThat(info1).isEqualTo(info2);
        assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
    }

    @Test
    @DisplayName("다른 연락처 정보를 가진 ContactInfo는 다르다고 판단된다")
    void inequalityTest() {
        // Given
        PhoneNumber primaryPhone1 = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber primaryPhone2 = PhoneNumber.ofKorean("010-9999-8888");

        ContactInfo info1 = ContactInfo.of(primaryPhone1);
        ContactInfo info2 = ContactInfo.of(primaryPhone2);

        // When & Then
        assertThat(info1).isNotEqualTo(info2);
    }

    @Test
    @DisplayName("보조 연락처가 다르면 다르다고 판단된다")
    void inequalityTestWithDifferentSecondaryPhone() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber secondaryPhone1 = PhoneNumber.of("+82", "02-1111-2222");
        PhoneNumber secondaryPhone2 = PhoneNumber.of("+82", "02-3333-4444");

        ContactInfo info1 = ContactInfo.of(primaryPhone, secondaryPhone1);
        ContactInfo info2 = ContactInfo.of(primaryPhone, secondaryPhone2);

        // When & Then
        assertThat(info1).isNotEqualTo(info2);
    }

    @Test
    @DisplayName("toString 메서드가 올바르게 동작한다")
    void toStringTest() {
        // Given
        PhoneNumber primaryPhone = PhoneNumber.ofKorean("010-1234-5678");
        PhoneNumber secondaryPhone = PhoneNumber.of("+82", "02-9876-5432");
        ContactInfo contactInfo = ContactInfo.of(primaryPhone, secondaryPhone);

        // When
        String result = contactInfo.toString();

        // Then
        assertThat(result).contains("primaryPhone");
        assertThat(result).contains("secondaryPhone");
        assertThat(result).doesNotContain("emergencyPhone"); // emergencyPhone이 제거되었는지 확인
    }
}