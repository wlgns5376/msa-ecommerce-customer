package com.commerce.customer.core.domain.service.profile;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.*;
import com.commerce.customer.core.domain.repository.profile.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CustomerProfileDomainService {
    
    private static final Pattern KOREAN_PHONE_PATTERN = Pattern.compile("^010-\\d{4}-\\d{4}$");
    
    private final CustomerProfileRepository profileRepository;
    private final AddressValidationService addressValidationService;

    /**
     * 새 프로필을 생성합니다.
     * 고객 ID 중복을 검사하고 프로필을 생성합니다.
     */
    public CustomerProfile createProfile(CustomerId customerId, PersonalInfo personalInfo, ContactInfo contactInfo) {
        // 중복 프로필 검사
        if (profileRepository.existsByCustomerId(customerId)) {
            throw new IllegalArgumentException("이미 프로필이 존재하는 고객입니다.");
        }
        
        // 연락처 유효성 검증
        validatePhoneNumber(contactInfo.getPrimaryPhone());
        
        // 프로필 생성
        CustomerProfile profile = CustomerProfile.create(customerId, personalInfo, contactInfo);
        
        return profileRepository.save(profile);
    }

    /**
     * 개인정보를 업데이트합니다.
     */
    public CustomerProfile updatePersonalInfo(ProfileId profileId, PersonalInfo personalInfo) {
        CustomerProfile profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));
        
        profile.updatePersonalInfo(personalInfo);
        
        return profileRepository.save(profile);
    }

    /**
     * 주소를 검증하고 추가합니다.
     */
    public CustomerProfile validateAndAddAddress(ProfileId profileId, Address address) {
        CustomerProfile profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));
        
        // 주소 유효성 검증
        validateAddress(address);
        
        profile.addAddress(address);
        
        return profileRepository.save(profile);
    }

    /**
     * 기본 주소를 설정합니다.
     */
    public CustomerProfile setDefaultAddress(ProfileId profileId, AddressId addressId) {
        CustomerProfile profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));
        
        profile.setDefaultAddress(addressId);
        
        return profileRepository.save(profile);
    }

    /**
     * 선호도를 업데이트합니다.
     */
    public CustomerProfile updatePreferences(ProfileId profileId, ProfilePreferences preferences) {
        CustomerProfile profile = profileRepository.findById(profileId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다."));
        
        profile.updatePreferences(preferences);
        
        return profileRepository.save(profile);
    }

    /**
     * 한국 전화번호 형식을 검증합니다.
     */
    public void validatePhoneNumber(PhoneNumber phoneNumber) {
        if ("+82".equals(phoneNumber.getCountryCode())) {
            if (!KOREAN_PHONE_PATTERN.matcher(phoneNumber.getNumber()).matches()) {
                throw new IllegalArgumentException("올바르지 않은 한국 전화번호 형식입니다.");
            }
        }
    }

    /**
     * 주소를 검증합니다.
     */
    public void validateAddress(Address address) {
        boolean isValid = addressValidationService.validateAddress(
            address.getZipCode(), 
            address.getRoadAddress()
        );
        
        if (!isValid) {
            throw new IllegalArgumentException("유효하지 않은 주소입니다.");
        }
    }
}