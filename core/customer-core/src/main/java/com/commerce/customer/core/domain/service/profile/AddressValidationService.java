package com.commerce.customer.core.domain.service.profile;

import java.util.List;

public interface AddressValidationService {
    
    /**
     * 주소의 유효성을 검증합니다.
     * 
     * @param zipCode 우편번호
     * @param roadAddress 도로명 주소
     * @return 유효성 여부
     */
    boolean validateAddress(String zipCode, String roadAddress);
    
    /**
     * 부분 주소로 주소 목록을 제안합니다.
     * 
     * @param partialAddress 부분 주소
     * @return 제안된 주소 목록
     */
    List<String> suggestAddresses(String partialAddress);
    
    /**
     * 우편번호로 주소 상세 정보를 조회합니다.
     * 
     * @param zipCode 우편번호
     * @return 주소 상세 정보
     */
    AddressDetails getAddressDetails(String zipCode);
    
    /**
     * 주소 상세 정보
     */
    record AddressDetails(
        String zipCode,
        String city,
        String district,
        String roadName,
        String buildingNumbers
    ) {}
}