package com.commerce.customer.core.domain.service;

import com.commerce.customer.core.domain.model.*;
import com.commerce.customer.core.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountDomainService {
    
    private final AccountRepository accountRepository;

    /**
     * 새 계정을 생성합니다.
     * 이메일 중복을 검사하고 계정을 생성합니다.
     */
    public Account createAccount(CustomerId customerId, Email email, Password rawPassword, 
                               PasswordEncoder passwordEncoder) {
        
        // 이메일 중복 검사
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + email.getValue());
        }
        
        // 비밀번호 암호화
        Password encodedPassword = Password.ofEncoded(passwordEncoder.encode(rawPassword.getValue()));
        
        // 계정 생성
        Account account = Account.create(customerId, email, encodedPassword);
        
        return accountRepository.save(account);
    }

    /**
     * 로그인을 시도합니다.
     * 계정 상태, 잠금 상태, 비밀번호를 검증합니다.
     */
    public LoginResult attemptLogin(Email email, Password rawPassword, 
                                  PasswordEncoder passwordEncoder) {
        
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다: " + email.getValue()));
        
        // 계정 잠금 상태 확인
        if (account.isLocked()) {
            return LoginResult.locked(account.getAccountId());
        }
        
        // 계정 상태 확인
        if (!account.getStatus().canLogin()) {
            return LoginResult.invalidStatus(account.getAccountId(), account.getStatus());
        }
        
        // 비밀번호 검증
        if (!passwordEncoder.matches(rawPassword.getValue(), account.getPassword().getValue())) {
            account.recordFailedLogin();
            accountRepository.save(account);
            return LoginResult.wrongPassword(account.getAccountId());
        }
        
        // 로그인 성공
        account.recordSuccessfulLogin();
        accountRepository.save(account);
        
        return LoginResult.success(account.getAccountId(), account.getCustomerId());
    }

    /**
     * 비밀번호를 변경합니다.
     */
    public void changePassword(AccountId accountId, Password currentPassword, 
                             Password newPassword, PasswordEncoder passwordEncoder) {
        
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다: " + accountId.getValue()));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword.getValue(), account.getPassword().getValue())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호 암호화 및 변경
        Password encodedNewPassword = Password.ofEncoded(passwordEncoder.encode(newPassword.getValue()));
        account.changePassword(encodedNewPassword);
        
        accountRepository.save(account);
    }

    /**
     * 계정을 활성화합니다.
     */
    public void activateAccount(AccountId accountId) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 계정입니다: " + accountId.getValue()));
        
        account.activate();
        accountRepository.save(account);
    }

    /**
     * 비밀번호 인코더 인터페이스
     */
    public interface PasswordEncoder {
        String encode(String rawPassword);
        boolean matches(String rawPassword, String encodedPassword);
    }

    /**
     * 로그인 결과
     */
    public static class LoginResult {
        private final boolean success;
        private final AccountId accountId;
        private final CustomerId customerId;
        private final String failureReason;
        private final AccountStatus accountStatus;

        private LoginResult(boolean success, AccountId accountId, CustomerId customerId, 
                          String failureReason, AccountStatus accountStatus) {
            this.success = success;
            this.accountId = accountId;
            this.customerId = customerId;
            this.failureReason = failureReason;
            this.accountStatus = accountStatus;
        }

        public static LoginResult success(AccountId accountId, CustomerId customerId) {
            return new LoginResult(true, accountId, customerId, null, null);
        }

        public static LoginResult locked(AccountId accountId) {
            return new LoginResult(false, accountId, null, "계정이 잠겨있습니다.", null);
        }

        public static LoginResult invalidStatus(AccountId accountId, AccountStatus status) {
            return new LoginResult(false, accountId, null, "로그인할 수 없는 계정 상태입니다.", status);
        }

        public static LoginResult wrongPassword(AccountId accountId) {
            return new LoginResult(false, accountId, null, "비밀번호가 일치하지 않습니다.", null);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public AccountId getAccountId() { return accountId; }
        public CustomerId getCustomerId() { return customerId; }
        public String getFailureReason() { return failureReason; }
        public AccountStatus getAccountStatus() { return accountStatus; }
    }
}