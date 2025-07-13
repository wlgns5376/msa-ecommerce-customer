package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressJpaRepository extends JpaRepository<AddressEntity, Long> {

    List<AddressEntity> findByCustomerProfile_CustomerId(Long customerId);

    Optional<AddressEntity> findByCustomerProfile_CustomerIdAndIsDefaultTrue(Long customerId);

    @Query("SELECT a FROM AddressEntity a WHERE a.customerProfile.customerId = :customerId AND a.addressId = :addressId")
    Optional<AddressEntity> findByCustomerIdAndAddressId(@Param("customerId") Long customerId, 
                                                        @Param("addressId") Long addressId);

    @Modifying
    @Query("UPDATE AddressEntity a SET a.isDefault = false WHERE a.customerProfile.customerId = :customerId")
    void unsetAllDefaultAddresses(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(a) FROM AddressEntity a WHERE a.customerProfile.customerId = :customerId")
    int countByCustomerId(@Param("customerId") Long customerId);
}