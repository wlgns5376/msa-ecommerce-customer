package com.commerce.customer.api.controller;

import com.commerce.customer.api.dto.profile.AddressRequest;
import com.commerce.customer.api.dto.profile.CreateProfileRequest;
import com.commerce.customer.api.dto.profile.CreateProfileResponse;
import com.commerce.customer.api.dto.profile.ProfileResponse;
import com.commerce.customer.api.dto.profile.UpdateProfileRequest;
import com.commerce.customer.api.dto.profile.UpdateProfileResponse;
import com.commerce.customer.api.exception.ResourceNotFoundException;
import com.commerce.customer.core.application.service.CustomerProfileApplicationService;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.profile.Address;
import com.commerce.customer.core.domain.model.profile.AddressId;
import com.commerce.customer.core.domain.model.profile.AddressType;
import com.commerce.customer.core.domain.model.profile.BirthDate;
import com.commerce.customer.core.domain.model.profile.ContactInfo;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.FullName;
import com.commerce.customer.core.domain.model.profile.Gender;
import com.commerce.customer.core.domain.model.profile.PersonalInfo;
import com.commerce.customer.core.domain.model.profile.PhoneNumber;
import com.commerce.customer.core.domain.model.profile.ProfileId;
import com.commerce.customer.core.domain.model.jwt.JwtClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "고객 프로필", description = "고객 프로필 관리")
@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Slf4j
public class CustomerProfileController {
    
    private final CustomerProfileApplicationService customerProfileApplicationService;
    
    @Operation(summary = "프로필 생성", description = "새로운 고객 프로필을 생성합니다.")
    @PostMapping
    public ResponseEntity<CreateProfileResponse> createProfile(
            @Valid @RequestBody CreateProfileRequest request,
            HttpServletRequest httpRequest) {
        
        JwtClaims jwtClaims = (JwtClaims) httpRequest.getAttribute("jwtClaims");
        if (jwtClaims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long accountId = Long.valueOf(jwtClaims.getAccountId());
        
        FullName fullName = FullName.of(request.getFirstName(), request.getLastName());
        BirthDate birthDate = request.getBirthDate() != null ? BirthDate.of(request.getBirthDate()) : null;
        Gender gender = request.getGender() != null ? Gender.valueOf(request.getGender()) : null;
        
        PersonalInfo personalInfo = PersonalInfo.of(fullName, birthDate, gender);
        
        PhoneNumber phoneNumber = request.getPhoneNumber() != null ? PhoneNumber.ofKorean(request.getPhoneNumber()) : null;
        ContactInfo contactInfo = ContactInfo.of(phoneNumber);
        
        ProfileId profileId = customerProfileApplicationService.createProfile(
                AccountId.of(accountId), personalInfo, contactInfo);
        
        CreateProfileResponse response = new CreateProfileResponse(
                profileId.getValue(),
                "프로필이 성공적으로 생성되었습니다."
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "프로필 조회", description = "현재 로그인된 계정의 프로필을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(HttpServletRequest request) {
        JwtClaims jwtClaims = (JwtClaims) request.getAttribute("jwtClaims");
        if (jwtClaims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long accountId = Long.valueOf(jwtClaims.getAccountId());
        
        try {
            CustomerProfile profile = customerProfileApplicationService.getProfileByAccountId(AccountId.of(accountId));
            return ResponseEntity.ok(ProfileResponse.from(profile));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("프로필을 찾을 수 없습니다")) {
                throw new ResourceNotFoundException(e.getMessage());
            }
            throw e;
        }
    }
    
    @Operation(summary = "프로필 조회 (ID)", description = "프로필 ID로 프로필을 조회합니다.")
    @GetMapping("/{profileId}")
    public ResponseEntity<CustomerProfile> getProfile(@PathVariable Long profileId) {
        
        CustomerProfile profile = customerProfileApplicationService.getProfile(ProfileId.of(profileId));
        
        return ResponseEntity.ok(profile);
    }
    
    @Operation(summary = "프로필 수정", description = "현재 로그인된 계정의 프로필을 수정합니다.")
    @PatchMapping
    public ResponseEntity<UpdateProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest) {
        
        JwtClaims jwtClaims = (JwtClaims) httpRequest.getAttribute("jwtClaims");
        if (jwtClaims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Long accountId = Long.valueOf(jwtClaims.getAccountId());
        
        try {
            // 전화번호 업데이트
            if (request.getPhoneNumber() != null) {
                PhoneNumber phoneNumber = PhoneNumber.ofKorean(request.getPhoneNumber());
                customerProfileApplicationService.updatePhoneNumber(AccountId.of(accountId), phoneNumber);
            }
            
            // TODO: 다른 필드 업데이트 로직 추가 (주소, 알림 설정, 마케팅 동의 등)
            
            return ResponseEntity.ok(UpdateProfileResponse.success());
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("프로필을 찾을 수 없습니다")) {
                throw new ResourceNotFoundException(e.getMessage());
            }
            throw e;
        }
    }
    
    @Operation(summary = "주소 추가", description = "프로필에 새로운 주소를 추가합니다.")
    @PostMapping("/{profileId}/addresses")
    public ResponseEntity<Void> addAddress(
            @PathVariable Long profileId,
            @Valid @RequestBody AddressRequest request) {
        
        Address address = Address.create(
                AddressType.valueOf(request.getType()),
                "기본 주소", // 별칭 기본값
                request.getZipCode(),
                request.getRoadAddress(),
                request.getJibunAddress(),
                request.getDetailAddress()
        );
        
        // 기본 주소 설정이 필요한 경우
        if (request.isDefault()) {
            address.setAsDefault();
        }
        
        customerProfileApplicationService.addAddress(ProfileId.of(profileId), address);
        
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @Operation(summary = "주소 수정", description = "기존 주소를 수정합니다.")
    @PutMapping("/{profileId}/addresses/{addressId}")
    public ResponseEntity<Void> updateAddress(
            @PathVariable Long profileId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        
        Address address = Address.create(
                AddressType.valueOf(request.getType()),
                "수정된 주소", // 별칭 기본값
                request.getZipCode(),
                request.getRoadAddress(),
                request.getJibunAddress(),
                request.getDetailAddress()
        );
        
        // 기본 주소 설정이 필요한 경우
        if (request.isDefault()) {
            address.setAsDefault();
        }
        
        customerProfileApplicationService.updateAddress(ProfileId.of(profileId), address);
        
        return ResponseEntity.ok().build();
    }
}