package com.commerce.customer.api.controller;

import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.CreateAccountResponse;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.customer.api.dto.account.LoginResponse;
import com.commerce.customer.api.dto.account.RefreshTokenRequest;
import com.commerce.customer.api.dto.account.RefreshTokenResponse;
import com.commerce.customer.core.application.service.AccountApplicationService;
import com.commerce.customer.core.domain.model.Account;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.Password;
import com.commerce.customer.core.domain.model.jwt.TokenPair;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "계정 관리", description = "계정 생성, 로그인, 로그아웃, 토큰 갱신")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    
    private final AccountApplicationService accountApplicationService;
    
    @Operation(summary = "계정 생성", description = "새로운 고객 계정을 생성합니다.")
    @PostMapping
    public ResponseEntity<CreateAccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {
        
        Email email = Email.of(request.getEmail());
        Password password = Password.of(request.getPassword());
        
        AccountId accountId = accountApplicationService.createAccount(email, password);
        
        CreateAccountResponse response = new CreateAccountResponse(
                accountId.getValue(),
                "계정이 성공적으로 생성되었습니다."
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        
        Email email = Email.of(request.getEmail());
        Password password = Password.of(request.getPassword());
        
        TokenPair tokenPair = accountApplicationService.login(email, password);
        
        LoginResponse response = new LoginResponse(
                tokenPair.getAccessToken().getValue(),
                tokenPair.getRefreshToken().getValue(),
                extractAccountIdFromToken(tokenPair.getAccessToken().getValue()),
                email.getValue()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "로그아웃", description = "현재 세션을 종료합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            accountApplicationService.logout(token);
        }
        
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        TokenPair tokenPair = accountApplicationService.refreshToken(request.getRefreshToken());
        
        RefreshTokenResponse response = new RefreshTokenResponse(
                tokenPair.getAccessToken().getValue(),
                tokenPair.getRefreshToken().getValue()
        );
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "계정 정보 조회", description = "현재 로그인된 계정의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<Account> getMyAccount(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long accountId = extractAccountIdFromToken(token);
            Account account = accountApplicationService.getAccount(AccountId.of(accountId));
            return ResponseEntity.ok(account);
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    
    private Long extractAccountIdFromToken(String token) {
        // JWT 토큰에서 accountId를 추출하는 로직
        // 실제 구현에서는 JwtTokenService를 사용해야 합니다.
        return 1L; // 임시 구현
    }
}