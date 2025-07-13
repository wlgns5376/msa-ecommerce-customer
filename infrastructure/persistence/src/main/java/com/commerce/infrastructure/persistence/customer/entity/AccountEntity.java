package com.commerce.infrastructure.persistence.customer.entity;

import com.commerce.infrastructure.persistence.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_email", columnList = "email", unique = true),
    @Index(name = "idx_account_customer_id", columnList = "customer_id", unique = true),
    @Index(name = "idx_account_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "customer_id", nullable = false, unique = true)
    private Long customerId;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public AccountEntity(Long customerId, String email, String password, 
                        AccountStatus status, LocalDateTime activatedAt, 
                        LocalDateTime lastLoginAt) {
        this.customerId = customerId;
        this.email = email;
        this.password = password;
        this.status = status;
        this.activatedAt = activatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    public void updateStatus(AccountStatus status) {
        this.status = status;
        if (status == AccountStatus.ACTIVE && this.activatedAt == null) {
            this.activatedAt = LocalDateTime.now();
        }
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public enum AccountStatus {
        PENDING, ACTIVE, SUSPENDED, DEACTIVATED
    }
}