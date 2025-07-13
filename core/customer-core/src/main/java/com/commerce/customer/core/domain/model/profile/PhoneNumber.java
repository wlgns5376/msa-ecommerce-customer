package com.commerce.customer.core.domain.model.profile;

import lombok.Getter;

import java.util.Objects;
import java.util.regex.Pattern;

@Getter
public class PhoneNumber {
    private static final Pattern KOREAN_PHONE_PATTERN = Pattern.compile("^010-\\d{4}-\\d{4}$");
    
    private final String countryCode;
    private final String number;

    private PhoneNumber(String countryCode, String number) {
        validateCountryCode(countryCode);
        validateNumber(number);
        
        this.countryCode = countryCode;
        this.number = number;
    }

    public static PhoneNumber of(String countryCode, String number) {
        return new PhoneNumber(countryCode, number);
    }

    public static PhoneNumber ofKorean(String number) {
        validateKoreanNumberFormat(number);
        return new PhoneNumber("+82", number);
    }

    public String getFormattedNumber() {
        return countryCode + " " + number;
    }

    private void validateCountryCode(String countryCode) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("국가코드는 필수값입니다.");
        }
    }

    private void validateNumber(String number) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("전화번호는 필수값입니다.");
        }
    }

    private static void validateKoreanNumberFormat(String number) {
        if (number == null || !KOREAN_PHONE_PATTERN.matcher(number).matches()) {
            throw new IllegalArgumentException("올바르지 않은 한국 전화번호 형식입니다.");
        }
        
        // 모든 자릿수가 0인 경우 체크
        if ("010-0000-0000".equals(number)) {
            throw new IllegalArgumentException("올바르지 않은 한국 전화번호 형식입니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return Objects.equals(countryCode, that.countryCode) && 
               Objects.equals(number, that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryCode, number);
    }

    @Override
    public String toString() {
        return getFormattedNumber();
    }
}