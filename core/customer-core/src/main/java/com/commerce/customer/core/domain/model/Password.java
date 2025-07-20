package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class Password {
    private static final Pattern PASSWORD_PATTERN = 
        Pattern.compile("^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s]).{8,}$");
    
    private final String value;

    private Password(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("비밀번호는 필수값입니다.");
        }
        
        if (!PASSWORD_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "비밀번호는 최소 8자 이상이며, 영문자, 숫자, 특수문자를 포함해야 합니다.");
        }
        
        this.value = value;
    }
    
    // 암호화된 비밀번호를 위한 생성자 (유효성 검증 없음)
    private Password(String encodedValue, boolean encoded) {
        if (encodedValue == null || encodedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("암호화된 비밀번호는 필수값입니다.");
        }
        this.value = encodedValue;
    }

    public static Password of(String value) {
        return new Password(value);
    }

    public static Password ofEncoded(String encodedValue) {
        return new Password(encodedValue, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(value, password.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "Password{hidden}";
    }
}