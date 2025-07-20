package com.commerce.infrastructure.persistence.customer.adapter;

import com.commerce.customer.core.domain.model.*;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.commerce.infrastructure.persistence.customer.mapper.AccountMapper;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.commerce.infrastructure.persistence.customer.repository.AccountQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private static final AtomicLong CUSTOMER_ID_SEQUENCE = new AtomicLong(1);
    
    private final AccountJpaRepository accountJpaRepository;
    private final AccountQueryRepository accountQueryRepository;
    private final AccountMapper accountMapper;
    
    @Override
    public CustomerId generateCustomerId() {
        return CustomerId.of(CUSTOMER_ID_SEQUENCE.getAndIncrement());
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity;
        
        // ID가 있는 경우 기존 엔티티를 조회하여 업데이트
        if (account.getAccountId() != null && account.getAccountId().isAssigned()) {
            entity = accountJpaRepository.findById(account.getAccountId().getValue())
                    .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다: " + account.getAccountId().getValue()));
            
            // 엔티티 필드 업데이트
            entity.updateStatus(AccountEntity.AccountStatus.valueOf(account.getStatus().name()));
            if (account.getLastLoginAt() != null) {
                entity.updateLastLoginAt(account.getLastLoginAt());
            }
            if (account.getActivationCode() != null) {
                entity.updateActivationCode(account.getActivationCode().getCode(), account.getActivationCode().getExpiresAt());
            }
            // 활성화된 경우 활성화 시간 설정
            if (account.getStatus() == AccountStatus.ACTIVE && entity.getActivatedAt() == null) {
                entity.updateActivatedAt(java.time.LocalDateTime.now());
            }
        } else {
            // 새로운 엔티티 생성
            entity = accountMapper.toEntity(account);
        }
        
        AccountEntity savedEntity = accountJpaRepository.save(entity);
        return accountMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return accountQueryRepository.findById(accountId.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return accountQueryRepository.findByEmail(email.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByCustomerId(CustomerId customerId) {
        return accountQueryRepository.findByCustomerId(customerId.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return accountQueryRepository.existsByEmail(email.getValue());
    }

    @Override
    public boolean existsByCustomerId(CustomerId customerId) {
        return accountQueryRepository.existsByCustomerId(customerId.getValue());
    }

    @Override
    public void delete(Account account) {
        if (!account.getAccountId().isAssigned()) {
            throw new IllegalStateException("ID가 할당되지 않은 계정은 삭제할 수 없습니다.");
        }
        
        // Soft Delete: 물리적 삭제 대신 논리적 삭제 수행
        accountJpaRepository.findById(account.getAccountId().getValue())
            .ifPresent(entity -> {
                entity.markAsDeleted();
                accountJpaRepository.save(entity);
            });
    }

    @Override
    public Optional<Account> findActiveByEmail(Email email) {
        return accountQueryRepository.findActiveAccountByEmail(email.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findActiveByCustomerId(CustomerId customerId) {
        return accountQueryRepository.findActiveAccountByCustomerId(customerId.getValue())
                .map(accountMapper::toDomain);
    }
}