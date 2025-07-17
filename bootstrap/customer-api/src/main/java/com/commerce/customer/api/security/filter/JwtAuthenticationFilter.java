package com.commerce.customer.api.security.filter;

import com.commerce.customer.core.domain.model.jwt.JwtClaims;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.service.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenService jwtTokenService;
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractTokenFromHeader(request);
        
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(token, request);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    private void authenticateUser(String tokenString, HttpServletRequest request) {
        try {
            Optional<JwtToken> jwtToken = jwtTokenService.parseToken(tokenString);
            
            if (jwtToken.isEmpty()) {
                log.debug("JWT 토큰 파싱 실패: {}", tokenString);
                return;
            }
            
            Optional<JwtClaims> claims = jwtTokenService.validateToken(jwtToken.get());
            
            if (claims.isEmpty()) {
                log.debug("JWT 토큰 검증 실패: {}", tokenString);
                return;
            }
            
            if (jwtTokenService.isTokenBlacklisted(jwtToken.get())) {
                log.debug("블랙리스트에 등록된 토큰: {}", tokenString);
                return;
            }
            
            JwtClaims jwtClaims = claims.get();
            UserDetails userDetails = User.builder()
                    .username(jwtClaims.getEmail())
                    .password("")
                    .authorities(Collections.emptyList())
                    .build();
            
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            
            // 인증 컨텍스트에 사용자 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // 요청 속성에 JWT Claims 저장 (컨트롤러에서 사용할 수 있도록)
            request.setAttribute("jwtClaims", jwtClaims);
            
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // 인증이 필요하지 않은 경로들
        return path.equals("/api/v1/accounts") && "POST".equals(request.getMethod()) ||
               path.equals("/api/v1/accounts/login") ||
               path.equals("/api/v1/accounts/refresh") ||
               path.startsWith("/swagger") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/actuator");
    }
}