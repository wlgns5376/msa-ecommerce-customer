package com.commerce.customer.api.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "프로필 수정 요청")
public class UpdateProfileRequest {
    
    @Schema(description = "전화번호", example = "010-9876-5432")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식이어야 합니다.")
    private String phoneNumber;
    
    @Schema(description = "주소 정보")
    private AddressRequest address;
    
    @Schema(description = "알림 설정")
    private NotificationSettingsRequest notificationSettings;
    
    @Schema(description = "마케팅 동의")
    private MarketingConsentRequest marketingConsent;
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "주소 정보")
    public static class AddressRequest {
        @Schema(description = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
        private String streetAddress;
        
        @Schema(description = "상세 주소", example = "456호")
        private String detailAddress;
        
        @Schema(description = "우편번호", example = "06234")
        @Pattern(regexp = "^\\d{5}$", message = "우편번호는 5자리 숫자여야 합니다.")
        private String postalCode;
        
        @Schema(description = "지번 주소", example = "서울특별시 강남구 역삼동 123-45")
        private String jibunAddress;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "알림 설정")
    public static class NotificationSettingsRequest {
        @Schema(description = "이메일 알림 허용", example = "true")
        private Boolean emailNotification;
        
        @Schema(description = "SMS 알림 허용", example = "true")
        private Boolean smsNotification;
        
        @Schema(description = "푸시 알림 허용", example = "true")
        private Boolean pushNotification;
    }
    
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "마케팅 동의")
    public static class MarketingConsentRequest {
        @Schema(description = "이메일 마케팅 동의", example = "true")
        private Boolean emailMarketing;
        
        @Schema(description = "SMS 마케팅 동의", example = "true")
        private Boolean smsMarketing;
    }
}