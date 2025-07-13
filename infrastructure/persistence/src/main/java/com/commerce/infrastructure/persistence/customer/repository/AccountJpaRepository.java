package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    @Query("SELECT a FROM AccountEntity a WHERE a.email = :email AND a.deleted = false")
    Optional<AccountEntity> findByEmail(@Param("email") String email);

    @Query("SELECT a FROM AccountEntity a WHERE a.customerId = :customerId AND a.deleted = false")
    Optional<AccountEntity> findByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AccountEntity a WHERE a.email = :email AND a.deleted = false")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AccountEntity a WHERE a.customerId = :customerId AND a.deleted = false")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT a FROM AccountEntity a WHERE a.email = :email AND a.status = 'ACTIVE' AND a.deleted = false")
    Optional<AccountEntity> findActiveAccountByEmail(@Param("email") String email);

    @Query("SELECT a FROM AccountEntity a WHERE a.customerId = :customerId AND a.status = 'ACTIVE' AND a.deleted = false")
    Optional<AccountEntity> findActiveAccountByCustomerId(@Param("customerId") Long customerId);

    /**
     * ID로 조회 시에도 삭제된 항목 제외
     */
    @Query("SELECT a FROM AccountEntity a WHERE a.accountId = :id AND a.deleted = false")
    Optional<AccountEntity> findById(@Param("id") Long id);
}