package com.commerce.customer.core.domain.service.jwt;

import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.jwt.JwtClaims;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.model.jwt.TokenPair;

import java.util.Optional;

public interface JwtTokenService {

    /**
     * 고객 정보를 기반으로 액세스 토큰과 리프레시 토큰 쌍을 생성합니다.
     */
    TokenPair generateTokenPair(CustomerId customerId, AccountId accountId, Email email);

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 생성합니다.
     */
    JwtToken refreshAccessToken(JwtToken refreshToken);

    /**
     * JWT 토큰을 검증하고 Claims를 반환합니다.
     */
    Optional<JwtClaims> validateToken(JwtToken token);

    /**
     * JWT 토큰 문자열을 파싱하여 JwtToken 객체로 변환합니다.
     */
    Optional<JwtToken> parseToken(String tokenString);

    /**
     * 토큰을 블랙리스트에 추가합니다 (로그아웃 시 사용).
     */
    void invalidateToken(JwtToken token);

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     */
    boolean isTokenBlacklisted(JwtToken token);

    /**
     * 특정 사용자의 모든 토큰을 무효화합니다.
     */
    void invalidateAllUserTokens(CustomerId customerId);
}