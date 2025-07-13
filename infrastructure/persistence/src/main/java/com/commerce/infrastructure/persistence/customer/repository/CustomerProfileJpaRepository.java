package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.CustomerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerProfileJpaRepository extends JpaRepository<CustomerProfileEntity, Long> {

    Optional<CustomerProfileEntity> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);

    @Query("SELECT cp FROM CustomerProfileEntity cp " +
           "LEFT JOIN FETCH cp.addresses a " +
           "LEFT JOIN FETCH cp.brandPreferences bp " +
           "LEFT JOIN FETCH cp.categoryInterests ci " +
           "WHERE cp.customerId = :customerId")
    Optional<CustomerProfileEntity> findByCustomerIdWithDetails(@Param("customerId") Long customerId);

    @Query("SELECT cp FROM CustomerProfileEntity cp " +
           "LEFT JOIN FETCH cp.addresses " +
           "WHERE cp.customerId = :customerId")
    Optional<CustomerProfileEntity> findByCustomerIdWithAddresses(@Param("customerId") Long customerId);

    @Query("SELECT cp FROM CustomerProfileEntity cp WHERE cp.customerId = :customerId AND cp.status = 'ACTIVE'")
    Optional<CustomerProfileEntity> findActiveProfileByCustomerId(@Param("customerId") Long customerId);
}