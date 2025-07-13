package com.commerce.customer.core.domain.model.jwt;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class JwtClaims {
    private final String subject; // 주체 (고객 ID)
    private final String accountId;
    private final String email;
    private final String issuer;
    private final String audience;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private final JwtTokenType tokenType;

    private JwtClaims(String subject, String accountId, String email, String issuer, 
                     String audience, LocalDateTime issuedAt, LocalDateTime expiresAt, 
                     JwtTokenType tokenType) {
        this.subject = Objects.requireNonNull(subject, "Subject는 필수입니다.");
        this.accountId = Objects.requireNonNull(accountId, "Account ID는 필수입니다.");
        this.email = Objects.requireNonNull(email, "Email은 필수입니다.");
        this.issuer = Objects.requireNonNull(issuer, "Issuer는 필수입니다.");
        this.audience = Objects.requireNonNull(audience, "Audience는 필수입니다.");
        this.issuedAt = Objects.requireNonNull(issuedAt, "발급 시간은 필수입니다.");
        this.expiresAt = Objects.requireNonNull(expiresAt, "만료 시간은 필수입니다.");
        this.tokenType = Objects.requireNonNull(tokenType, "토큰 타입은 필수입니다.");
    }

    public static JwtClaims create(CustomerId customerId, AccountId accountId, Email email,
                                 String issuer, String audience, JwtTokenType tokenType) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(tokenType.getExpirationMinutes());

        return new JwtClaims(
            customerId.getValue(),
            accountId.getValue(),
            email.getValue(),
            issuer,
            audience,
            now,
            expiresAt,
            tokenType
        );
    }

    public static JwtClaims of(String subject, String accountId, String email, String issuer,
                             String audience, LocalDateTime issuedAt, LocalDateTime expiresAt,
                             JwtTokenType tokenType) {
        return new JwtClaims(subject, accountId, email, issuer, audience, issuedAt, expiresAt, tokenType);
    }

    public CustomerId getCustomerId() {
        return CustomerId.of(subject);
    }

    public AccountId getAccountIdObject() {
        return AccountId.of(accountId);
    }

    public Email getEmailObject() {
        return Email.of(email);
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtClaims jwtClaims = (JwtClaims) o;
        return Objects.equals(subject, jwtClaims.subject) &&
               Objects.equals(accountId, jwtClaims.accountId) &&
               Objects.equals(tokenType, jwtClaims.tokenType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, accountId, tokenType);
    }
}