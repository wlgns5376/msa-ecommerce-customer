package com.commerce.infrastructure.persistence.security.filter;

import com.commerce.customer.core.domain.service.jwt.JwtTokenService;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.model.jwt.JwtClaims;
import com.commerce.infrastructure.persistence.security.service.RedisTokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final RedisTokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String tokenString = extractTokenFromRequest(request);
            
            if (tokenString != null) {
                // 토큰 파싱
                Optional<JwtToken> tokenOpt = jwtTokenService.parseToken(tokenString);
                if (tokenOpt.isPresent()) {
                    JwtToken token = tokenOpt.get();
                    
                    // 토큰 검증
                    Optional<JwtClaims> claimsOpt = jwtTokenService.validateToken(token);
                    if (claimsOpt.isPresent()) {
                        // 블랙리스트 확인
                        if (tokenBlacklistService.isTokenBlacklisted(tokenString)) {
                            log.warn("Blacklisted token attempt: {}", tokenString.substring(0, Math.min(tokenString.length(), 20)));
                        } else {
                            // 인증 정보 설정
                            Authentication authentication = createAuthentication(claimsOpt.get());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            log.debug("JWT authentication successful for token: {}", 
                                     tokenString.substring(0, Math.min(tokenString.length(), 20)));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    private Authentication createAuthentication(JwtClaims claims) {
        String email = claims.getEmailObject().getValue();
        Long customerId = claims.getCustomerId().getValue();
        
        // 간단한 Principal 객체 생성 (실제로는 UserDetails 구현체 사용 권장)
        JwtAuthenticationPrincipal principal = new JwtAuthenticationPrincipal(email, customerId);
        
        return new UsernamePasswordAuthenticationToken(
            principal, 
            null, // 인증된 상태에서는 credentials를 null로 설정
            Collections.emptyList() // 권한은 필요에 따라 토큰에서 추출
        );
    }

    // JWT 인증 Principal 클래스
    public record JwtAuthenticationPrincipal(String email, Long customerId) {
    }
}