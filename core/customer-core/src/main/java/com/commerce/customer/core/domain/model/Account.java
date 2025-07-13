package com.commerce.customer.core.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class Account {
    private final AccountId accountId;
    private final CustomerId customerId;
    private final Email email;
    private Password password;
    private AccountStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private int loginFailCount;
    private LocalDateTime lockedUntil;

    private final List<Object> domainEvents = new ArrayList<>();

    private Account(AccountId accountId, CustomerId customerId, Email email, 
                   Password password, AccountStatus status, LocalDateTime createdAt) {
        this.accountId = Objects.requireNonNull(accountId, "계정 ID는 필수값입니다.");
        this.customerId = Objects.requireNonNull(customerId, "고객 ID는 필수값입니다.");
        this.email = Objects.requireNonNull(email, "이메일은 필수값입니다.");
        this.password = Objects.requireNonNull(password, "비밀번호는 필수값입니다.");
        this.status = Objects.requireNonNull(status, "계정 상태는 필수값입니다.");
        this.createdAt = Objects.requireNonNull(createdAt, "생성일시는 필수값입니다.");
        this.updatedAt = createdAt;
        this.loginFailCount = 0;
    }

    public static Account create(CustomerId customerId, Email email, Password password) {
        AccountId accountId = AccountId.generate();
        LocalDateTime now = LocalDateTime.now();
        
        Account account = new Account(accountId, customerId, email, password, 
                                    AccountStatus.PENDING, now);
        
        // 도메인 이벤트 발행 (구현 예정)
        // account.addDomainEvent(new AccountCreatedEvent(accountId, customerId, email));
        
        return account;
    }

    public void activate() {
        if (!status.canActivate()) {
            throw new IllegalStateException("현재 상태에서는 활성화할 수 없습니다: " + status);
        }
        
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new AccountActivatedEvent(accountId, customerId));
    }

    public void deactivate() {
        if (!status.canDeactivate()) {
            throw new IllegalStateException("현재 상태에서는 비활성화할 수 없습니다: " + status);
        }
        
        this.status = AccountStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new AccountDeactivatedEvent(accountId, customerId));
    }

    public void changePassword(Password newPassword) {
        if (!AccountStatus.ACTIVE.equals(status)) {
            throw new IllegalStateException("활성 상태에서만 비밀번호를 변경할 수 있습니다.");
        }
        
        this.password = Objects.requireNonNull(newPassword, "새 비밀번호는 필수값입니다.");
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new PasswordChangedEvent(accountId, customerId));
    }

    public void recordSuccessfulLogin() {
        if (!status.canLogin()) {
            throw new IllegalStateException("로그인할 수 없는 상태입니다: " + status);
        }
        
        this.lastLoginAt = LocalDateTime.now();
        this.loginFailCount = 0;
        this.lockedUntil = null;
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new LoginSuccessfulEvent(accountId, customerId, lastLoginAt));
    }

    public void recordFailedLogin() {
        this.loginFailCount++;
        this.updatedAt = LocalDateTime.now();
        
        // 3회 실패 시 30분 잠금
        if (loginFailCount >= 3) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new LoginFailedEvent(accountId, customerId, loginFailCount));
    }

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public void delete() {
        if (!status.canDelete()) {
            throw new IllegalStateException("이미 삭제된 계정입니다.");
        }
        
        this.status = AccountStatus.DELETED;
        this.updatedAt = LocalDateTime.now();
        
        // 도메인 이벤트 발행 (구현 예정)
        // addDomainEvent(new AccountDeletedEvent(accountId, customerId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(accountId, account.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
}