package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class Email {
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9]([A-Za-z0-9+_.'-]*[A-Za-z0-9])?@[A-Za-z0-9]([A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}$");
    
    private final String value;

    private Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수값입니다.");
        }
        
        String trimmedValue = value.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + value);
        }
        
        // 연속된 점 체크
        if (trimmedValue.contains("..")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다: " + value);
        }
        
        this.value = trimmedValue;
    }

    public static Email of(String value) {
        return new Email(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}