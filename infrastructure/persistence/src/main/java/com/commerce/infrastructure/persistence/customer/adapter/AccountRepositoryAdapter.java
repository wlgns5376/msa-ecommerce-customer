package com.commerce.infrastructure.persistence.customer.adapter;

import com.commerce.customer.core.domain.model.*;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.commerce.infrastructure.persistence.customer.mapper.AccountMapper;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account save(Account account) {
        AccountEntity entity = accountMapper.toEntity(account);
        AccountEntity savedEntity = accountJpaRepository.save(entity);
        return accountMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Account> findById(AccountId accountId) {
        return accountJpaRepository.findById(accountId.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByEmail(Email email) {
        return accountJpaRepository.findByEmail(email.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByCustomerId(CustomerId customerId) {
        return accountJpaRepository.findByCustomerId(customerId.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return accountJpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public boolean existsByCustomerId(CustomerId customerId) {
        return accountJpaRepository.existsByCustomerId(customerId.getValue());
    }

    @Override
    public void delete(Account account) {
        accountJpaRepository.deleteById(account.getAccountId().getValue());
    }

    @Override
    public Optional<Account> findActiveByEmail(Email email) {
        return accountJpaRepository.findActiveAccountByEmail(email.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    public Optional<Account> findActiveByCustomerId(CustomerId customerId) {
        return accountJpaRepository.findActiveAccountByCustomerId(customerId.getValue())
                .map(accountMapper::toDomain);
    }
}