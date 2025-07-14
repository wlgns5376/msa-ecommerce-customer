package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.commerce.infrastructure.persistence.customer.entity.QAccountEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Account QueryDSL Repository
 */
@Repository
@RequiredArgsConstructor
public class AccountQueryRepository {

    private final JPAQueryFactory queryFactory;
    private final QAccountEntity account = QAccountEntity.accountEntity;

    /**
     * 이메일로 활성 계정 조회 (삭제되지 않은 계정만)
     */
    public Optional<AccountEntity> findByEmail(String email) {
        AccountEntity result = queryFactory
                .selectFrom(account)
                .where(
                        account.email.eq(email)
                                .and(account.deleted.eq(false))
                )
                .fetchOne();
        
        return Optional.ofNullable(result);
    }

    /**
     * 고객 ID로 활성 계정 조회 (삭제되지 않은 계정만)
     */
    public Optional<AccountEntity> findByCustomerId(Long customerId) {
        AccountEntity result = queryFactory
                .selectFrom(account)
                .where(
                        account.customerId.eq(customerId)
                                .and(account.deleted.eq(false))
                )
                .fetchOne();
        
        return Optional.ofNullable(result);
    }

    /**
     * 이메일 존재 여부 확인 (삭제되지 않은 계정만)
     */
    public boolean existsByEmail(String email) {
        Integer count = queryFactory
                .selectOne()
                .from(account)
                .where(
                        account.email.eq(email)
                                .and(account.deleted.eq(false))
                )
                .fetchFirst();
        
        return count != null;
    }

    /**
     * 고객 ID 존재 여부 확인 (삭제되지 않은 계정만)
     */
    public boolean existsByCustomerId(Long customerId) {
        Integer count = queryFactory
                .selectOne()
                .from(account)
                .where(
                        account.customerId.eq(customerId)
                                .and(account.deleted.eq(false))
                )
                .fetchFirst();
        
        return count != null;
    }

    /**
     * 이메일로 활성 상태의 계정 조회 (삭제되지 않고 ACTIVE 상태인 계정만)
     */
    public Optional<AccountEntity> findActiveAccountByEmail(String email) {
        AccountEntity result = queryFactory
                .selectFrom(account)
                .where(
                        account.email.eq(email)
                                .and(account.status.eq(AccountEntity.AccountStatus.ACTIVE))
                                .and(account.deleted.eq(false))
                )
                .fetchOne();
        
        return Optional.ofNullable(result);
    }

    /**
     * 고객 ID로 활성 상태의 계정 조회 (삭제되지 않고 ACTIVE 상태인 계정만)
     */
    public Optional<AccountEntity> findActiveAccountByCustomerId(Long customerId) {
        AccountEntity result = queryFactory
                .selectFrom(account)
                .where(
                        account.customerId.eq(customerId)
                                .and(account.status.eq(AccountEntity.AccountStatus.ACTIVE))
                                .and(account.deleted.eq(false))
                )
                .fetchOne();
        
        return Optional.ofNullable(result);
    }

    /**
     * ID로 활성 계정 조회 (삭제되지 않은 계정만)
     */
    public Optional<AccountEntity> findById(Long id) {
        AccountEntity result = queryFactory
                .selectFrom(account)
                .where(
                        account.accountId.eq(id)
                                .and(account.deleted.eq(false))
                )
                .fetchOne();
        
        return Optional.ofNullable(result);
    }
}