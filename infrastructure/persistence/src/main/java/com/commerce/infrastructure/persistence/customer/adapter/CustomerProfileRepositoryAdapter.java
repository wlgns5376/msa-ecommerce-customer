package com.commerce.infrastructure.persistence.customer.adapter;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.ProfileId;
import com.commerce.customer.core.domain.repository.profile.CustomerProfileRepository;
import com.commerce.infrastructure.persistence.customer.entity.CustomerProfileEntity;
import com.commerce.infrastructure.persistence.customer.mapper.CustomerProfileMapper;
import com.commerce.infrastructure.persistence.customer.repository.CustomerProfileJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomerProfileRepositoryAdapter implements CustomerProfileRepository {

    private final CustomerProfileJpaRepository customerProfileJpaRepository;
    private final CustomerProfileMapper customerProfileMapper;

    @Override
    public CustomerProfile save(CustomerProfile customerProfile) {
        CustomerProfileEntity entity;
        
        // 기존 프로필이 있는지 CustomerId로 확인
        Optional<CustomerProfileEntity> existingEntity = 
            customerProfileJpaRepository.findByCustomerId(customerProfile.getCustomerId().getValue());
        
        if (existingEntity.isPresent()) {
            // 기존 엔티티 업데이트
            entity = existingEntity.get();
            updateEntityFromDomain(entity, customerProfile);
        } else {
            // 새로운 엔티티 생성
            entity = customerProfileMapper.toEntity(customerProfile);
        }
        
        CustomerProfileEntity savedEntity = customerProfileJpaRepository.save(entity);
        return customerProfileMapper.toDomain(savedEntity);
    }
    
    private void updateEntityFromDomain(CustomerProfileEntity entity, CustomerProfile profile) {
        // PersonalInfo 업데이트
        entity.updatePersonalInfo(
                profile.getPersonalInfo().getFullName().getFirstName(),
                profile.getPersonalInfo().getFullName().getLastName(),
                profile.getPersonalInfo().getBirthDate() != null ? profile.getPersonalInfo().getBirthDate().getDate() : null,
                profile.getPersonalInfo().getGender() != null ? 
                    CustomerProfileEntity.Gender.valueOf(profile.getPersonalInfo().getGender().name()) : null,
                null // profileImageUrl은 별도 관리
        );
        
        // ContactInfo 업데이트
        entity.updateContactInfo(
                profile.getContactInfo().getPrimaryPhone().getNumber(),
                profile.getContactInfo().getSecondaryPhone() != null ? 
                    profile.getContactInfo().getSecondaryPhone().getNumber() : null
        );
        
        // MarketingConsent 업데이트
        entity.updateMarketingConsent(
                profile.getPreferences().getMarketingConsent().isEmailMarketing(),
                profile.getPreferences().getMarketingConsent().isSmsMarketing(),
                profile.getPreferences().getMarketingConsent().isPersonalizedAds()
        );
        
        // NotificationSettings 업데이트
        entity.updateNotificationSettings(
                profile.getPreferences().getNotificationSettings().isOrderUpdates(),
                profile.getPreferences().getNotificationSettings().isPromotionalOffers(),
                profile.getPreferences().getNotificationSettings().isEmailNotification(),
                profile.getPreferences().getNotificationSettings().isSmsNotification()
        );
    }

    @Override
    public Optional<CustomerProfile> findById(ProfileId profileId) {
        return customerProfileJpaRepository.findById(profileId.getValue())
                .map(customerProfileMapper::toDomain);
    }

    @Override
    public Optional<CustomerProfile> findByCustomerId(CustomerId customerId) {
        return customerProfileJpaRepository.findByCustomerId(customerId.getValue())
                .map(customerProfileMapper::toDomain);
    }

    @Override
    public boolean existsByCustomerId(CustomerId customerId) {
        return customerProfileJpaRepository.existsByCustomerId(customerId.getValue());
    }

    @Override
    public void delete(CustomerProfile customerProfile) {
        customerProfileJpaRepository.deleteById(customerProfile.getProfileId().getValue());
    }

    @Override
    public Optional<CustomerProfile> findActiveByCustomerId(CustomerId customerId) {
        return customerProfileJpaRepository.findActiveProfileByCustomerId(customerId.getValue())
                .map(customerProfileMapper::toDomain);
    }

    @Override
    public Optional<CustomerProfile> findByCustomerIdWithAddresses(CustomerId customerId) {
        return customerProfileJpaRepository.findByCustomerIdWithAddresses(customerId.getValue())
                .map(customerProfileMapper::toDomain);
    }

    @Override
    public void deleteById(ProfileId profileId) {
        customerProfileJpaRepository.deleteById(profileId.getValue());
    }
}