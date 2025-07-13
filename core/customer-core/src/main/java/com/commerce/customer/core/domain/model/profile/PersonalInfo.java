package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;

@Getter
public class PersonalInfo {
    private final FullName fullName;
    private final BirthDate birthDate;
    private final Gender gender;
    private final ProfileImage profileImage;

    private PersonalInfo(FullName fullName, BirthDate birthDate, Gender gender, ProfileImage profileImage) {
        this.fullName = Objects.requireNonNull(fullName, "이름은 필수값입니다.");
        this.birthDate = birthDate; // nullable - 개인정보보호 고려
        this.gender = gender; // nullable - 개인정보보호 고려
        this.profileImage = profileImage; // nullable
    }

    public static PersonalInfo of(FullName fullName) {
        return new PersonalInfo(fullName, null, null, null);
    }

    public static PersonalInfo of(FullName fullName, BirthDate birthDate, Gender gender) {
        return new PersonalInfo(fullName, birthDate, gender, null);
    }

    public static PersonalInfo of(FullName fullName, BirthDate birthDate, Gender gender, ProfileImage profileImage) {
        return new PersonalInfo(fullName, birthDate, gender, profileImage);
    }

    public PersonalInfo updateName(FullName newName) {
        return new PersonalInfo(newName, this.birthDate, this.gender, this.profileImage);
    }

    public PersonalInfo updateBirthDate(BirthDate newBirthDate) {
        return new PersonalInfo(this.fullName, newBirthDate, this.gender, this.profileImage);
    }

    public PersonalInfo updateGender(Gender newGender) {
        return new PersonalInfo(this.fullName, this.birthDate, newGender, this.profileImage);
    }

    public PersonalInfo updateProfileImage(ProfileImage newProfileImage) {
        return new PersonalInfo(this.fullName, this.birthDate, this.gender, newProfileImage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalInfo that = (PersonalInfo) o;
        return Objects.equals(fullName, that.fullName) &&
               Objects.equals(birthDate, that.birthDate) &&
               gender == that.gender &&
               Objects.equals(profileImage, that.profileImage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fullName, birthDate, gender, profileImage);
    }

    @Override
    public String toString() {
        return "PersonalInfo{" +
                "fullName=" + fullName +
                ", birthDate=" + birthDate +
                ", gender=" + gender +
                ", profileImage=" + profileImage +
                '}';
    }
}