package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Getter
public class BirthDate {
    private final LocalDate date;

    private BirthDate(LocalDate date) {
        validateDate(date);
        this.date = date;
    }

    public static BirthDate of(LocalDate date) {
        return new BirthDate(date);
    }

    public int getAge() {
        return Period.between(date, LocalDate.now()).getYears();
    }

    public String getAgeGroup() {
        int age = getAge();
        if (age < 20) return "10대";
        if (age < 30) return "20대";
        if (age < 40) return "30대";
        if (age < 50) return "40대";
        if (age < 60) return "50대";
        return "60대 이상";
    }

    private void validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("생년월일은 필수값입니다.");
        }

        LocalDate today = LocalDate.now();
        
        if (date.isAfter(today)) {
            throw new IllegalArgumentException("생년월일은 미래일 수 없습니다.");
        }

        int age = Period.between(date, today).getYears();
        if (age < 14) {
            throw new IllegalArgumentException("만 14세 이상만 가입 가능합니다.");
        }

        if (age > 120) {
            throw new IllegalArgumentException("올바르지 않은 생년월일입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BirthDate birthDate = (BirthDate) o;
        return Objects.equals(date, birthDate.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }

    @Override
    public String toString() {
        return date.toString();
    }
}