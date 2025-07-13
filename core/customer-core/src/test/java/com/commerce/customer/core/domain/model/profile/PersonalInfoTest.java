package com.commerce.customer.core.domain.model.profile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PersonalInfo 값객체 테스트")
class PersonalInfoTest {

    @Test
    @DisplayName("이름만으로 PersonalInfo를 생성할 수 있다")
    void createPersonalInfoWithNameOnly() {
        // Given
        FullName fullName = FullName.of("홍", "길동");

        // When
        PersonalInfo personalInfo = PersonalInfo.of(fullName);

        // Then
        assertThat(personalInfo).isNotNull();
        assertThat(personalInfo.getFullName()).isEqualTo(fullName);
        assertThat(personalInfo.getBirthDate()).isNull();
        assertThat(personalInfo.getGender()).isNull();
        assertThat(personalInfo.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("이름, 생년월일, 성별로 PersonalInfo를 생성할 수 있다")
    void createPersonalInfoWithFullDetails() {
        // Given
        FullName fullName = FullName.of("홍", "길동");
        BirthDate birthDate = BirthDate.of(LocalDate.of(1990, 1, 1));
        Gender gender = Gender.MALE;

        // When
        PersonalInfo personalInfo = PersonalInfo.of(fullName, birthDate, gender);

        // Then
        assertThat(personalInfo).isNotNull();
        assertThat(personalInfo.getFullName()).isEqualTo(fullName);
        assertThat(personalInfo.getBirthDate()).isEqualTo(birthDate);
        assertThat(personalInfo.getGender()).isEqualTo(gender);
        assertThat(personalInfo.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("모든 정보로 PersonalInfo를 생성할 수 있다")
    void createPersonalInfoWithAllDetails() {
        // Given
        FullName fullName = FullName.of("홍", "길동");
        BirthDate birthDate = BirthDate.of(LocalDate.of(1990, 1, 1));
        Gender gender = Gender.MALE;
        ProfileImage profileImage = ProfileImage.of("https://example.com/profile.jpg");

        // When
        PersonalInfo personalInfo = PersonalInfo.of(fullName, birthDate, gender, profileImage);

        // Then
        assertThat(personalInfo).isNotNull();
        assertThat(personalInfo.getFullName()).isEqualTo(fullName);
        assertThat(personalInfo.getBirthDate()).isEqualTo(birthDate);
        assertThat(personalInfo.getGender()).isEqualTo(gender);
        assertThat(personalInfo.getProfileImage()).isEqualTo(profileImage);
    }

    @Test
    @DisplayName("이름을 업데이트할 수 있다")
    void updateName() {
        // Given
        FullName originalName = FullName.of("홍", "길동");
        FullName newName = FullName.of("김", "철수");
        PersonalInfo personalInfo = PersonalInfo.of(originalName);

        // When
        PersonalInfo updatedInfo = personalInfo.updateName(newName);

        // Then
        assertThat(updatedInfo.getFullName()).isEqualTo(newName);
        assertThat(updatedInfo.getBirthDate()).isNull();
        assertThat(updatedInfo.getGender()).isNull();
        assertThat(updatedInfo.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("생년월일을 업데이트할 수 있다")
    void updateBirthDate() {
        // Given
        FullName fullName = FullName.of("홍", "길동");
        BirthDate newBirthDate = BirthDate.of(LocalDate.of(1985, 5, 15));
        PersonalInfo personalInfo = PersonalInfo.of(fullName);

        // When
        PersonalInfo updatedInfo = personalInfo.updateBirthDate(newBirthDate);

        // Then
        assertThat(updatedInfo.getFullName()).isEqualTo(fullName);
        assertThat(updatedInfo.getBirthDate()).isEqualTo(newBirthDate);
        assertThat(updatedInfo.getGender()).isNull();
        assertThat(updatedInfo.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("성별을 업데이트할 수 있다")
    void updateGender() {
        // Given
        FullName fullName = FullName.of("홍", "길동");
        Gender newGender = Gender.FEMALE;
        PersonalInfo personalInfo = PersonalInfo.of(fullName);

        // When
        PersonalInfo updatedInfo = personalInfo.updateGender(newGender);

        // Then
        assertThat(updatedInfo.getFullName()).isEqualTo(fullName);
        assertThat(updatedInfo.getBirthDate()).isNull();
        assertThat(updatedInfo.getGender()).isEqualTo(newGender);
        assertThat(updatedInfo.getProfileImage()).isNull();
    }

    @Test
    @DisplayName("프로필 이미지를 업데이트할 수 있다")
    void updateProfileImage() {
        // Given
        FullName fullName = FullName.of("홍", "길동");
        ProfileImage newProfileImage = ProfileImage.of("https://example.com/new-profile.jpg");
        PersonalInfo personalInfo = PersonalInfo.of(fullName);

        // When
        PersonalInfo updatedInfo = personalInfo.updateProfileImage(newProfileImage);

        // Then
        assertThat(updatedInfo.getFullName()).isEqualTo(fullName);
        assertThat(updatedInfo.getBirthDate()).isNull();
        assertThat(updatedInfo.getGender()).isNull();
        assertThat(updatedInfo.getProfileImage()).isEqualTo(newProfileImage);
    }

    @Test
    @DisplayName("이름이 null이면 예외가 발생한다")
    void throwExceptionWhenNameIsNull() {
        // When & Then
        assertThatThrownBy(() -> PersonalInfo.of(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("이름은 필수값입니다.");
    }

    @Test
    @DisplayName("동일한 정보를 가진 PersonalInfo는 같다고 판단된다")
    void equalityTest() {
        // Given
        FullName fullName = FullName.of("홍", "길동");
        BirthDate birthDate = BirthDate.of(LocalDate.of(1990, 1, 1));
        Gender gender = Gender.MALE;
        ProfileImage profileImage = ProfileImage.of("https://example.com/profile.jpg");

        PersonalInfo info1 = PersonalInfo.of(fullName, birthDate, gender, profileImage);
        PersonalInfo info2 = PersonalInfo.of(fullName, birthDate, gender, profileImage);

        // When & Then
        assertThat(info1).isEqualTo(info2);
        assertThat(info1.hashCode()).isEqualTo(info2.hashCode());
    }

    @Test
    @DisplayName("다른 정보를 가진 PersonalInfo는 다르다고 판단된다")
    void inequalityTest() {
        // Given
        FullName fullName1 = FullName.of("홍", "길동");
        FullName fullName2 = FullName.of("김", "철수");

        PersonalInfo info1 = PersonalInfo.of(fullName1);
        PersonalInfo info2 = PersonalInfo.of(fullName2);

        // When & Then
        assertThat(info1).isNotEqualTo(info2);
    }
}