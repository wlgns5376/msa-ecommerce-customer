package com.commerce.customer.api.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressRequest {
    
    @Pattern(regexp = "^(HOME|WORK|OTHER)$", message = "주소 유형은 HOME, WORK, OTHER 중 하나여야 합니다.")
    private String type;
    
    @NotBlank(message = "도로명 주소는 필수입니다.")
    private String roadAddress;
    
    private String jibunAddress;
    
    @NotBlank(message = "상세 주소는 필수입니다.")
    private String detailAddress;
    
    @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
    private String zipCode;
    
    private boolean isDefault;
    
    public AddressRequest(String type, String roadAddress, String jibunAddress, 
                         String detailAddress, String zipCode, boolean isDefault) {
        this.type = type;
        this.roadAddress = roadAddress;
        this.jibunAddress = jibunAddress;
        this.detailAddress = detailAddress;
        this.zipCode = zipCode;
        this.isDefault = isDefault;
    }
}