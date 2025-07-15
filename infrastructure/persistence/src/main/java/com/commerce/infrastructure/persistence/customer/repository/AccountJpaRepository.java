package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Account JPA Repository
 * 복잡한 쿼리는 AccountQueryRepository(QueryDSL)을 사용
 */
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {
    // 기본 JPA 메서드만 사용
    // 복잡한 조건이 있는 쿼리는 AccountQueryRepository에서 처리
}