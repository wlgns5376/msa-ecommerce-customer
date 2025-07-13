package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;

@Getter
public class FullName {
    private final String firstName;
    private final String lastName;

    private FullName(String firstName, String lastName) {
        validateFirstName(firstName);
        validateLastName(lastName);
        validateLength(firstName, lastName);
        
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static FullName of(String firstName, String lastName) {
        return new FullName(firstName, lastName);
    }

    public String getDisplayName() {
        return lastName + firstName;
    }

    private void validateFirstName(String firstName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수값입니다.");
        }
    }

    private void validateLastName(String lastName) {
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new IllegalArgumentException("성은 필수값입니다.");
        }
    }

    private void validateLength(String firstName, String lastName) {
        if (firstName.length() > 50 || lastName.length() > 50) {
            throw new IllegalArgumentException("이름은 50자를 초과할 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullName fullName = (FullName) o;
        return Objects.equals(firstName, fullName.firstName) && 
               Objects.equals(lastName, fullName.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}