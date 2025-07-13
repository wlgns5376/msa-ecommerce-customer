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
        CustomerProfileEntity entity = customerProfileMapper.toEntity(customerProfile);
        CustomerProfileEntity savedEntity = customerProfileJpaRepository.save(entity);
        return customerProfileMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<CustomerProfile> findById(ProfileId profileId) {
        return customerProfileJpaRepository.findById(profileId.getValue())
                .map(customerProfileMapper::toDomain);
    }

    @Override
    public Optional<CustomerProfile> findByCustomerId(CustomerId customerId) {
        return customerProfileJpaRepository.findByCustomerIdWithDetails(customerId.getValue())
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