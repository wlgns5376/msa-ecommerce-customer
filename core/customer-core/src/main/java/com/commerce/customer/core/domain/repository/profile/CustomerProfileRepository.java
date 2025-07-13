package com.commerce.customer.core.domain.repository.profile;

import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.profile.CustomerProfile;
import com.commerce.customer.core.domain.model.profile.ProfileId;

import java.util.Optional;

public interface CustomerProfileRepository {
    
    /**
     * 프로필을 저장합니다.
     * 
     * @param profile 저장할 프로필
     * @return 저장된 프로필
     */
    CustomerProfile save(CustomerProfile profile);
    
    /**
     * 프로필 ID로 프로필을 조회합니다.
     * 
     * @param profileId 프로필 ID
     * @return 조회된 프로필 (Optional)
     */
    Optional<CustomerProfile> findById(ProfileId profileId);
    
    /**
     * 고객 ID로 프로필을 조회합니다.
     * 
     * @param customerId 고객 ID
     * @return 조회된 프로필 (Optional)
     */
    Optional<CustomerProfile> findByCustomerId(CustomerId customerId);
    
    /**
     * 고객 ID로 프로필 존재 여부를 확인합니다.
     * 
     * @param customerId 고객 ID
     * @return 존재 여부
     */
    boolean existsByCustomerId(CustomerId customerId);
    
    /**
     * 활성 상태의 프로필을 고객 ID로 조회합니다.
     * 
     * @param customerId 고객 ID
     * @return 조회된 프로필 (Optional)
     */
    Optional<CustomerProfile> findActiveByCustomerId(CustomerId customerId);
    
    /**
     * 고객 ID로 주소 정보를 포함한 프로필을 조회합니다.
     * 
     * @param customerId 고객 ID
     * @return 조회된 프로필 (Optional)
     */
    Optional<CustomerProfile> findByCustomerIdWithAddresses(CustomerId customerId);
    
    /**
     * 프로필을 삭제합니다.
     * 
     * @param profile 삭제할 프로필
     */
    void delete(CustomerProfile profile);
    
    /**
     * 프로필을 삭제합니다.
     * 
     * @param profileId 삭제할 프로필 ID
     */
    void deleteById(ProfileId profileId);
}