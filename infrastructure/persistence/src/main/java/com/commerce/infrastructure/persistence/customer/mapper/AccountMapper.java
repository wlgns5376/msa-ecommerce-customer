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
                .lastLoginAt(account.getLastLoginAt())
                .build();
    }

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        // accountId가 null인 경우 임시 ID 생성 (새로 생성된 엔티티의 경우)
        AccountId accountId = entity.getAccountId() != null 
            ? AccountId.of(entity.getAccountId()) 
            : AccountId.generate();

        return Account.restore(
                accountId,
                CustomerId.of(entity.getCustomerId()),
                Email.of(entity.getEmail()),
                Password.ofEncoded(entity.getPassword()), // 엔티티의 password는 이미 인코딩된 값
                mapToDomainStatus(entity.getStatus()),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : java.time.LocalDateTime.now(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : java.time.LocalDateTime.now(),
                entity.getLastLoginAt()
        );
    }

    private AccountEntity.AccountStatus mapToEntityStatus(AccountStatus domainStatus) {
        return switch (domainStatus) {
            case PENDING -> AccountEntity.AccountStatus.PENDING;
            case ACTIVE -> AccountEntity.AccountStatus.ACTIVE;
            case INACTIVE -> AccountEntity.AccountStatus.INACTIVE;
            case DORMANT -> AccountEntity.AccountStatus.DORMANT;
            case SUSPENDED -> AccountEntity.AccountStatus.SUSPENDED;
            case DELETED -> AccountEntity.AccountStatus.DELETED;
        };
    }

    private AccountStatus mapToDomainStatus(AccountEntity.AccountStatus entityStatus) {
        return switch (entityStatus) {
            case PENDING -> AccountStatus.PENDING;
            case ACTIVE -> AccountStatus.ACTIVE;
            case INACTIVE -> AccountStatus.INACTIVE;
            case DORMANT -> AccountStatus.DORMANT;
            case SUSPENDED -> AccountStatus.SUSPENDED;
            case DELETED -> AccountStatus.DELETED;
        };
    }
}