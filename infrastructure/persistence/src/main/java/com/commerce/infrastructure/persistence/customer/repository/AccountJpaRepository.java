package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByEmail(String email);

    Optional<AccountEntity> findByCustomerId(Long customerId);

    boolean existsByEmail(String email);

    boolean existsByCustomerId(Long customerId);

    @Query("SELECT a FROM AccountEntity a WHERE a.email = :email AND a.status = 'ACTIVE'")
    Optional<AccountEntity> findActiveAccountByEmail(@Param("email") String email);

    @Query("SELECT a FROM AccountEntity a WHERE a.customerId = :customerId AND a.status = 'ACTIVE'")
    Optional<AccountEntity> findActiveAccountByCustomerId(@Param("customerId") Long customerId);
}