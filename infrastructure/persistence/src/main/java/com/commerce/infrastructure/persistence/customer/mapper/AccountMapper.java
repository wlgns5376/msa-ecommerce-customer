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

        // ID가 할당되지 않은 새로운 Account의 경우 accountId를 설정하지 않음 (DB에서 auto-increment)
        AccountEntity.AccountEntityBuilder builder = AccountEntity.builder()
                .customerId(account.getCustomerId().getValue())
                .email(account.getEmail().getValue())
                .password(account.getPassword().getValue())
                .status(mapToEntityStatus(account.getStatus()))
                .lastLoginAt(account.getLastLoginAt());
        
        // 인증 코드 설정
        if (account.getActivationCode() != null) {
            builder.activationCode(account.getActivationCode().getCode())
                   .activationCodeExpiresAt(account.getActivationCode().getExpiresAt());
        }
        
        return builder.build();
    }

    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        // DB에서 조회된 Entity는 항상 유효한 accountId를 가져야 함
        if (entity.getAccountId() == null) {
            throw new IllegalStateException("Entity의 accountId가 null입니다. DB 조회 결과가 올바르지 않습니다.");
        }

        // 인증 코드 복원
        ActivationCode activationCode = null;
        if (entity.getActivationCode() != null && entity.getActivationCodeExpiresAt() != null) {
            activationCode = ActivationCode.of(entity.getActivationCode(), entity.getActivationCodeExpiresAt());
        }
        
        return Account.restore(
                AccountId.of(entity.getAccountId()),
                CustomerId.of(entity.getCustomerId()),
                Email.of(entity.getEmail()),
                Password.ofEncoded(entity.getPassword()), // 엔티티의 password는 이미 인코딩된 값
                mapToDomainStatus(entity.getStatus()),
                entity.getCreatedAt() != null ? entity.getCreatedAt() : java.time.LocalDateTime.now(),
                entity.getUpdatedAt() != null ? entity.getUpdatedAt() : java.time.LocalDateTime.now(),
                entity.getLastLoginAt(),
                activationCode
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