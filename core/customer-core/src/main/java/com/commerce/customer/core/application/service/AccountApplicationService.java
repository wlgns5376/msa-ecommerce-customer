package com.commerce.customer.core.application.service;

import com.commerce.customer.core.application.usecase.account.CreateAccountUseCase;
import com.commerce.customer.core.application.usecase.account.LoginUseCase;
import com.commerce.customer.core.application.usecase.account.LogoutUseCase;
import com.commerce.customer.core.application.usecase.account.RefreshTokenUseCase;
import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.Password;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.model.jwt.TokenPair;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.customer.core.domain.service.AccountDomainService;
import com.commerce.customer.core.domain.service.jwt.JwtTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountApplicationService implements CreateAccountUseCase, LoginUseCase, LogoutUseCase, RefreshTokenUseCase {
    
    private final AccountRepository accountRepository;
    private final AccountDomainService accountDomainService;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public AccountId createAccount(Email email, Password password) {
        CustomerId customerId = CustomerId.generate();
        Account account = accountDomainService.createAccount(customerId, email, password, 
                new AccountDomainService.PasswordEncoder() {
                    @Override
                    public String encode(String rawPassword) {
                        return passwordEncoder.encode(rawPassword);
                    }
                    
                    @Override
                    public boolean matches(String rawPassword, String encodedPassword) {
                        return passwordEncoder.matches(rawPassword, encodedPassword);
                    }
                });
        
        return account.getAccountId();
    }
    
    @Override
    public TokenPair login(Email email, Password password) {
        AccountDomainService.LoginResult loginResult = accountDomainService.attemptLogin(email, password,
                new AccountDomainService.PasswordEncoder() {
                    @Override
                    public String encode(String rawPassword) {
                        return passwordEncoder.encode(rawPassword);
                    }
                    
                    @Override
                    public boolean matches(String rawPassword, String encodedPassword) {
                        return passwordEncoder.matches(rawPassword, encodedPassword);
                    }
                });
        
        if (!loginResult.isSuccess()) {
            throw new IllegalArgumentException(loginResult.getFailureReason());
        }
        
        return jwtTokenService.generateTokenPair(
                loginResult.getCustomerId(), 
                loginResult.getAccountId(), 
                email);
    }
    
    @Override
    public void logout(String accessToken) {
        jwtTokenService.parseToken(accessToken).ifPresent(jwtTokenService::invalidateToken);
    }
    
    @Override
    public TokenPair refreshToken(String refreshToken) {
        return jwtTokenService.parseToken(refreshToken)
                .map(refreshJwtToken -> {
                    JwtToken newAccessToken = jwtTokenService.refreshAccessToken(refreshJwtToken);
                    return TokenPair.of(newAccessToken, refreshJwtToken);
                })
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));
    }
    
    @Override
    @Transactional(readOnly = true)
    public Account getAccount(AccountId accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));
    }
}