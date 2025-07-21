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
    @Index(name = "idx_account_email", columnList = "email, deleted", unique = true),
    @Index(name = "idx_account_customer_id", columnList = "customer_id, deleted", unique = true),
    @Index(name = "idx_account_status", columnList = "status"),
    @Index(name = "idx_account_deleted", columnList = "deleted")
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

    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "activation_code", length = 32)
    private String activationCode;

    @Column(name = "activation_code_expires_at")
    private LocalDateTime activationCodeExpiresAt;

    @Builder
    public AccountEntity(Long customerId, String email, String password, 
                        AccountStatus status, LocalDateTime activatedAt, 
                        LocalDateTime lastLoginAt, String activationCode,
                        LocalDateTime activationCodeExpiresAt) {
        this.customerId = customerId;
        this.email = email;
        this.password = password;
        this.status = status;
        this.activatedAt = activatedAt;
        this.lastLoginAt = lastLoginAt;
        this.activationCode = activationCode;
        this.activationCodeExpiresAt = activationCodeExpiresAt;
    }

    public void updateStatus(AccountStatus status) {
        this.status = status;
        if (status == AccountStatus.ACTIVE && this.activatedAt == null) {
            this.activatedAt = LocalDateTime.now();
            this.activationCode = null;
            this.activationCodeExpiresAt = null;
        }
    }

    public void updateLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
    
    public void updateActivationCode(String activationCode, LocalDateTime activationCodeExpiresAt) {
        this.activationCode = activationCode;
        this.activationCodeExpiresAt = activationCodeExpiresAt;
    }
    
    public void updateActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    /**
     * 논리적 삭제 (Soft Delete) 수행
     */
    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.status = AccountStatus.DELETED;
    }

    /**
     * 논리적 삭제 복원
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        // 상태는 비즈니스 로직에 따라 결정되므로 여기서는 변경하지 않음
    }

    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(this.deleted);
    }

    public enum AccountStatus {
        PENDING, ACTIVE, INACTIVE, DORMANT, SUSPENDED, DELETED
    }
}