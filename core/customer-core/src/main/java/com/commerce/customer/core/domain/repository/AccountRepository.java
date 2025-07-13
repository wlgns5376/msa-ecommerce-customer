package com.commerce.customer.core.domain.repository;

import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;

import java.util.Optional;

public interface AccountRepository {
    
    /**
     * 계정을 저장합니다.
     */
    Account save(Account account);
    
    /**
     * 계정 ID로 계정을 조회합니다.
     */
    Optional<Account> findById(AccountId accountId);
    
    /**
     * 고객 ID로 계정을 조회합니다.
     */
    Optional<Account> findByCustomerId(CustomerId customerId);
    
    /**
     * 이메일로 계정을 조회합니다.
     */
    Optional<Account> findByEmail(Email email);
    
    /**
     * 이메일 중복 여부를 확인합니다.
     */
    boolean existsByEmail(Email email);
    
    /**
     * 계정을 삭제합니다.
     */
    void delete(Account account);
}