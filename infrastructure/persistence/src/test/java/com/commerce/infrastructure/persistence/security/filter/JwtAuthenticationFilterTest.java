package com.commerce.infrastructure.persistence.security.filter;

import com.commerce.customer.core.domain.service.jwt.JwtTokenService;
import com.commerce.infrastructure.persistence.security.service.RedisTokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private RedisTokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.validtoken";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final String TEST_EMAIL = "test@example.com";
    private static final Long TEST_CUSTOMER_ID = 1L;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("유효한 JWT 토큰으로 인증에 성공한다")
    void doFilterInternal_ValidToken_Success() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        given(jwtTokenService.validateToken(VALID_TOKEN)).willReturn(true);
        given(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).willReturn(false);
        given(jwtTokenService.extractEmail(VALID_TOKEN)).willReturn(TEST_EMAIL);
        given(jwtTokenService.extractCustomerId(VALID_TOKEN)).willReturn(TEST_CUSTOMER_ID);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(securityContext).should(times(1)).setAuthentication(any(Authentication.class));
        then(filterChain).should(times(1)).doFilter(request, response);
        
        // Authentication 객체가 설정되었는지 확인
        verify(securityContext).setAuthentication(argThat(auth -> {
            assertThat(auth.getPrincipal()).isInstanceOf(JwtAuthenticationFilter.JwtAuthenticationPrincipal.class);
            JwtAuthenticationFilter.JwtAuthenticationPrincipal principal = 
                (JwtAuthenticationFilter.JwtAuthenticationPrincipal) auth.getPrincipal();
            assertThat(principal.email()).isEqualTo(TEST_EMAIL);
            assertThat(principal.customerId()).isEqualTo(TEST_CUSTOMER_ID);
            return true;
        }));
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증을 건너뛴다")
    void doFilterInternal_NoAuthorizationHeader_Skip() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(jwtTokenService).should(never()).validateToken(anyString());
        then(securityContext).should(never()).setAuthentication(any());
        then(filterChain).should(times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Bearer로 시작하지 않는 토큰은 인증을 건너뛴다")
    void doFilterInternal_InvalidTokenFormat_Skip() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn("Basic sometoken");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(jwtTokenService).should(never()).validateToken(anyString());
        then(securityContext).should(never()).setAuthentication(any());
        then(filterChain).should(times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰은 인증을 건너뛴다")
    void doFilterInternal_InvalidToken_Skip() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        given(jwtTokenService.validateToken(VALID_TOKEN)).willReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(tokenBlacklistService).should(never()).isTokenBlacklisted(anyString());
        then(securityContext).should(never()).setAuthentication(any());
        then(filterChain).should(times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("블랙리스트에 등록된 토큰은 인증을 거부한다")
    void doFilterInternal_BlacklistedToken_Reject() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        given(jwtTokenService.validateToken(VALID_TOKEN)).willReturn(true);
        given(tokenBlacklistService.isTokenBlacklisted(VALID_TOKEN)).willReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(jwtTokenService).should(never()).extractEmail(anyString());
        then(securityContext).should(never()).setAuthentication(any());
        then(filterChain).should(times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("JWT 처리 중 예외 발생 시 SecurityContext를 정리한다")
    void doFilterInternal_Exception_ClearContext() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
        given(jwtTokenService.validateToken(VALID_TOKEN)).willThrow(new RuntimeException("JWT error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(securityContext).should(times(1)).setAuthentication(null); // clearContext 호출
        then(filterChain).should(times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("빈 토큰 문자열은 인증을 건너뛴다")
    void doFilterInternal_EmptyToken_Skip() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn("Bearer ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(jwtTokenService).should(never()).validateToken(anyString());
        then(securityContext).should(never()).setAuthentication(any());
        then(filterChain).should(times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("whitespace만 있는 Authorization 헤더는 무시한다")
    void doFilterInternal_WhitespaceHeader_Skip() throws ServletException, IOException {
        // Given
        given(request.getHeader("Authorization")).willReturn("   ");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        then(jwtTokenService).should(never()).validateToken(anyString());
        then(securityContext).should(never()).setAuthentication(any());
        then(filterChain).should(times(1)).doFilter(request, response);
    }
}