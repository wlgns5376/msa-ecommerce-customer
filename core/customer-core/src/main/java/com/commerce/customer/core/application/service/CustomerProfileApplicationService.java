package com.commerce.customer.core.application.service;

import com.commerce.customer.core.application.usecase.profile.CreateCustomerProfileUseCase;
import com.commerce.customer.core.application.usecase.profile.GetCustomerProfileUseCase;
import com.commerce.customer.core.application.usecase.profile.UpdateCustomerProfileUseCase;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.ProfileId;
import com.commerce.customer.core.domain.model.profile.PersonalInfo;
import com.commerce.customer.core.domain.model.profile.ContactInfo;
import com.commerce.customer.core.domain.model.profile.Address;
import com.commerce.customer.core.domain.model.profile.AddressId;
import com.commerce.customer.core.domain.model.profile.ProfilePreferences;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.customer.core.domain.repository.profile.CustomerProfileRepository;
import com.commerce.customer.core.domain.service.profile.CustomerProfileDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerProfileApplicationService implements CreateCustomerProfileUseCase, GetCustomerProfileUseCase, UpdateCustomerProfileUseCase {
    
    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerProfileDomainService customerProfileDomainService;
    private final AccountRepository accountRepository;
    
    @Override
    public ProfileId createProfile(AccountId accountId, PersonalInfo personalInfo, ContactInfo contactInfo) {
        // AccountId로부터 CustomerId를 가져옵니다
        CustomerId customerId = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."))
                .getCustomerId();
        
        CustomerProfile profile = customerProfileDomainService.createProfile(customerId, personalInfo, contactInfo);
        return profile.getProfileId();
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomerProfile getProfile(ProfileId profileId) {
        return customerProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("프로필을 찾을 수 없습니다."));
    }
    
    @Override
    @Transactional(readOnly = true)
    public CustomerProfile getProfileByAccountId(AccountId accountId) {
        CustomerId customerId = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."))
                .getCustomerId();
        
        return customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new IllegalArgumentException("계정의 프로필을 찾을 수 없습니다."));
    }
    
    @Override
    public void updatePersonalInfo(ProfileId profileId, PersonalInfo personalInfo) {
        customerProfileDomainService.updatePersonalInfo(profileId, personalInfo);
    }
    
    @Override
    public void updateContactInfo(ProfileId profileId, ContactInfo contactInfo) {
        CustomerProfile profile = getProfile(profileId);
        profile.updateContactInfo(contactInfo);
        customerProfileRepository.save(profile);
    }
    
    @Override
    public void addAddress(ProfileId profileId, Address address) {
        customerProfileDomainService.validateAndAddAddress(profileId, address);
    }
    
    @Override
    public void updateAddress(ProfileId profileId, Address address) {
        CustomerProfile profile = getProfile(profileId);
        // 기존 주소를 제거하고 새 주소를 추가하는 방식으로 업데이트
        profile.removeAddress(address.getAddressId());
        profile.addAddress(address);
        customerProfileRepository.save(profile);
    }
    
    @Override
    public void removeAddress(ProfileId profileId, Address address) {
        CustomerProfile profile = getProfile(profileId);
        profile.removeAddress(address.getAddressId());
        customerProfileRepository.save(profile);
    }
    
    @Override
    public void updatePreferences(ProfileId profileId, ProfilePreferences preferences) {
        customerProfileDomainService.updatePreferences(profileId, preferences);
    }
}