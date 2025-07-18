package com.commerce.customer.api.domain.service.profile;

import com.commerce.customer.core.domain.service.profile.AddressValidationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

/**
 * 주소 검증 서비스 구현체
 * 실제 환경에서는 외부 주소 API (다음/카카오 API 등)를 사용하여 구현
 */
@Service
public class AddressValidationServiceImpl implements AddressValidationService {

    private static final Pattern ZIPCODE_PATTERN = Pattern.compile("^\\d{5}$");

    @Override
    public boolean validateAddress(String zipCode, String roadAddress) {
        if (zipCode == null || roadAddress == null) {
            return false;
        }
        
        // 우편번호 형식 검증 (5자리 숫자)
        if (!ZIPCODE_PATTERN.matcher(zipCode).matches()) {
            return false;
        }
        
        // 도로명 주소 기본 검증 (실제로는 외부 API 사용)
        return roadAddress.trim().length() >= 5;
    }

    @Override
    public List<String> suggestAddresses(String partialAddress) {
        if (partialAddress == null || partialAddress.trim().isEmpty()) {
            return List.of();
        }
        
        // 실제로는 외부 주소 API를 호출하여 제안 목록 반환
        // 현재는 더미 데이터 반환
        return List.of(
            partialAddress + " 1번길 10",
            partialAddress + " 2번길 20",
            partialAddress + " 3번길 30"
        );
    }

    @Override
    public AddressDetails getAddressDetails(String zipCode) {
        if (zipCode == null || !ZIPCODE_PATTERN.matcher(zipCode).matches()) {
            return null;
        }
        
        // 실제로는 외부 주소 API를 호출하여 주소 상세 정보 반환
        // 현재는 더미 데이터 반환
        return new AddressDetails(
            zipCode,
            "서울특별시",
            "강남구",
            "테헤란로",
            "100-200"
        );
    }
}