package com.commerce.customer.core.domain.model;

import com.commerce.customer.core.domain.event.AccountActivatedEvent;
import com.commerce.customer.core.domain.event.AccountCreatedEvent;
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
    private ActivationCode activationCode;

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
        // ID는 persistence 계층에서 할당받도록 null로 생성
        AccountId accountId = AccountId.newInstance();
        LocalDateTime now = LocalDateTime.now();
        
        Account account = new Account(accountId, customerId, email, password, 
                                    AccountStatus.PENDING, now);
        
        // 계정 활성화를 위한 인증 코드 생성
        account.activationCode = ActivationCode.generate();
        
        // 도메인 이벤트 발행은 ID 할당 후에 수행 (persistence 계층에서)
        // account.addDomainEvent(new AccountCreatedEvent(accountId, customerId, email, account.activationCode.getCode()));
        
        return account;
    }

    /**
     * 영속성 계층에서 데이터를 복원할 때 사용하는 팩토리 메서드
     */
    public static Account restore(AccountId accountId, CustomerId customerId, Email email, 
                                Password password, AccountStatus status, LocalDateTime createdAt,
                                LocalDateTime updatedAt, LocalDateTime lastLoginAt, 
                                ActivationCode activationCode) {
        Account account = new Account(accountId, customerId, email, password, status, createdAt);
        account.updatedAt = updatedAt;
        account.lastLoginAt = lastLoginAt;
        account.activationCode = activationCode;
        return account;
    }

    /**
     * Persistence 계층에서 새로 할당된 ID로 도메인 객체를 재생성
     * (새로 생성된 Account의 ID 할당용)
     */
    public Account withAssignedId(AccountId assignedId) {
        if (this.accountId.isAssigned()) {
            throw new IllegalStateException("이미 ID가 할당된 계정입니다.");
        }
        if (!assignedId.isAssigned()) {
            throw new IllegalArgumentException("할당할 ID가 유효하지 않습니다.");
        }
        
        return Account.restore(assignedId, this.customerId, this.email, this.password, 
                             this.status, this.createdAt, this.updatedAt, this.lastLoginAt, 
                             this.activationCode);
    }

    public void activate(String inputCode) {
        if (!status.canActivate()) {
            throw new IllegalStateException("현재 상태에서는 활성화할 수 없습니다: " + status);
        }
        
        if (activationCode == null) {
            throw new IllegalStateException("인증 코드가 생성되지 않았습니다.");
        }
        
        if (activationCode.isExpired()) {
            throw new IllegalStateException("인증 코드가 만료되었습니다.");
        }
        
        if (!activationCode.matches(inputCode)) {
            throw new IllegalArgumentException("잘못된 인증 코드입니다.");
        }
        
        this.status = AccountStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        this.activationCode = null; // 사용된 인증 코드는 제거
        
        // 도메인 이벤트 발행
        raiseAccountActivatedEvent();
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
    
    public void raiseAccountCreatedEvent() {
        if (activationCode != null) {
            domainEvents.add(new AccountCreatedEvent(accountId, customerId, email, activationCode.getCode()));
        }
    }
    
    public void raiseAccountActivatedEvent() {
        domainEvents.add(new AccountActivatedEvent(accountId, customerId));
    }
    
    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }
    
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}