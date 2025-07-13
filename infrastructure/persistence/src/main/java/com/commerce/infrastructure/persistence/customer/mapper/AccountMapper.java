package com.commerce.infrastructure.persistence.customer.mapper;

import com.commerce.customer.core.domain.model.*;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountEntity toEntity(Account account) {
        if (account == null) {
            return null;
        }

        return AccountEntity.builder()
                .customerId(account.getCustomerId().getValue())
                .email(account.getEmail().getValue())
                .password(account.getPassword().getValue())
                .status(mapToEntityStatus(account.getStatus()))
                .activatedAt(account.getActivatedAt())
                .lastLoginAt(account.getLastLoginAt())
                .build();
    }

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        return Account.builder()
                .accountId(AccountId.of(entity.getAccountId()))
                .customerId(CustomerId.of(entity.getCustomerId()))
                .email(Email.of(entity.getEmail()))
                .password(Password.of(entity.getPassword()))
                .status(mapToDomainStatus(entity.getStatus()))
                .activatedAt(entity.getActivatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }

    private AccountEntity.AccountStatus mapToEntityStatus(AccountStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> AccountEntity.AccountStatus.PENDING;
            case ACTIVE -> AccountEntity.AccountStatus.ACTIVE;
            case SUSPENDED -> AccountEntity.AccountStatus.SUSPENDED;
            case DEACTIVATED -> AccountEntity.AccountStatus.DEACTIVATED;
        };
    }

    private AccountStatus mapToDomainStatus(AccountEntity.AccountStatus entityStatus) {
        return switch (entityStatus) {
            case PENDING -> AccountStatus.PENDING;
            case ACTIVE -> AccountStatus.ACTIVE;
            case SUSPENDED -> AccountStatus.SUSPENDED;
            case DEACTIVATED -> AccountStatus.DEACTIVATED;
        };
    }
}