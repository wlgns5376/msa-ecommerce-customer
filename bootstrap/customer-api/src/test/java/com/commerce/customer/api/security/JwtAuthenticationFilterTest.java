package com.commerce.customer.api.security;

import com.commerce.customer.api.security.filter.JwtAuthenticationFilter;
import com.commerce.customer.core.domain.model.jwt.JwtClaims;
import com.commerce.customer.core.domain.model.jwt.JwtToken;
import com.commerce.customer.core.domain.service.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationFilter 단위 테스트")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenService jwtTokenService;

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

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
    private static final String BEARER_TOKEN = "Bearer " + VALID_TOKEN;
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Nested
    @DisplayName("doFilterInternal 메서드 테스트")
    class DoFilterInternalTest {

        @Test
        @DisplayName("유효한 JWT 토큰으로 인증 성공")
        void givenValidToken_whenDoFilterInternal_thenAuthenticationSucceeds() throws ServletException, IOException {
            // Given
            JwtToken jwtToken = mock(JwtToken.class);
            JwtClaims jwtClaims = mock(JwtClaims.class);
            
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(securityContext.getAuthentication()).willReturn(null);
            given(jwtTokenService.parseToken(VALID_TOKEN)).willReturn(Optional.of(jwtToken));
            given(jwtTokenService.validateToken(jwtToken)).willReturn(Optional.of(jwtClaims));
            given(jwtTokenService.isTokenBlacklisted(jwtToken)).willReturn(false);
            given(jwtClaims.getEmail()).willReturn(EMAIL);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should().parseToken(VALID_TOKEN);
            then(jwtTokenService).should().validateToken(jwtToken);
            then(jwtTokenService).should().isTokenBlacklisted(jwtToken);
            then(securityContext).should().setAuthentication(any(Authentication.class));
            then(request).should().setAttribute(eq("jwtClaims"), eq(jwtClaims));
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("Authorization 헤더가 없을 때 필터 통과")
        void givenNoAuthorizationHeader_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn(null);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should(never()).parseToken(anyString());
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("Bearer 접두사가 없는 토큰일 때 필터 통과")
        void givenTokenWithoutBearerPrefix_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn(VALID_TOKEN);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should(never()).parseToken(anyString());
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("이미 인증된 사용자일 때 필터 통과")
        void givenAlreadyAuthenticated_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            Authentication existingAuth = mock(Authentication.class);
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(securityContext.getAuthentication()).willReturn(existingAuth);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should(never()).parseToken(anyString());
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("토큰 파싱 실패 시 필터 통과")
        void givenTokenParsingFails_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(securityContext.getAuthentication()).willReturn(null);
            given(jwtTokenService.parseToken(VALID_TOKEN)).willReturn(Optional.empty());

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should().parseToken(VALID_TOKEN);
            then(jwtTokenService).should(never()).validateToken(any());
            then(securityContext).should(never()).setAuthentication(any());
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("토큰 검증 실패 시 필터 통과")
        void givenTokenValidationFails_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            JwtToken jwtToken = mock(JwtToken.class);
            
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(securityContext.getAuthentication()).willReturn(null);
            given(jwtTokenService.parseToken(VALID_TOKEN)).willReturn(Optional.of(jwtToken));
            given(jwtTokenService.validateToken(jwtToken)).willReturn(Optional.empty());

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should().parseToken(VALID_TOKEN);
            then(jwtTokenService).should().validateToken(jwtToken);
            then(jwtTokenService).should(never()).isTokenBlacklisted(any());
            then(securityContext).should(never()).setAuthentication(any());
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("블랙리스트에 등록된 토큰일 때 필터 통과")
        void givenBlacklistedToken_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            JwtToken jwtToken = mock(JwtToken.class);
            JwtClaims jwtClaims = mock(JwtClaims.class);
            
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(securityContext.getAuthentication()).willReturn(null);
            given(jwtTokenService.parseToken(VALID_TOKEN)).willReturn(Optional.of(jwtToken));
            given(jwtTokenService.validateToken(jwtToken)).willReturn(Optional.of(jwtClaims));
            given(jwtTokenService.isTokenBlacklisted(jwtToken)).willReturn(true);

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should().parseToken(VALID_TOKEN);
            then(jwtTokenService).should().validateToken(jwtToken);
            then(jwtTokenService).should().isTokenBlacklisted(jwtToken);
            then(securityContext).should(never()).setAuthentication(any());
            then(filterChain).should().doFilter(request, response);
        }

        @Test
        @DisplayName("JWT 처리 중 예외 발생 시 필터 통과")
        void givenExceptionDuringJwtProcessing_whenDoFilterInternal_thenFilterPasses() throws ServletException, IOException {
            // Given
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);
            given(securityContext.getAuthentication()).willReturn(null);
            given(jwtTokenService.parseToken(VALID_TOKEN)).willThrow(new RuntimeException("JWT 처리 오류"));

            // When
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Then
            then(jwtTokenService).should().parseToken(VALID_TOKEN);
            then(securityContext).should(never()).setAuthentication(any());
            then(filterChain).should().doFilter(request, response);
        }
    }

    @Nested
    @DisplayName("shouldNotFilter 메서드 테스트")
    class ShouldNotFilterTest {

        @Test
        @DisplayName("계정 생성 API는 필터링하지 않음")
        void givenAccountCreationRequest_whenShouldNotFilter_thenReturnTrue() {
            // Given
            given(request.getRequestURI()).willReturn("/api/v1/accounts");
            given(request.getMethod()).willReturn("POST");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("로그인 API는 필터링하지 않음")
        void givenLoginRequest_whenShouldNotFilter_thenReturnTrue() {
            // Given
            given(request.getRequestURI()).willReturn("/api/v1/accounts/login");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("토큰 갱신 API는 필터링하지 않음")
        void givenRefreshTokenRequest_whenShouldNotFilter_thenReturnTrue() {
            // Given
            given(request.getRequestURI()).willReturn("/api/v1/accounts/refresh");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Swagger UI는 필터링하지 않음")
        void givenSwaggerRequest_whenShouldNotFilter_thenReturnTrue() {
            // Given
            given(request.getRequestURI()).willReturn("/swagger-ui/index.html");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("API Docs는 필터링하지 않음")
        void givenApiDocsRequest_whenShouldNotFilter_thenReturnTrue() {
            // Given
            given(request.getRequestURI()).willReturn("/v3/api-docs/swagger-config");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Actuator는 필터링하지 않음")
        void givenActuatorRequest_whenShouldNotFilter_thenReturnTrue() {
            // Given
            given(request.getRequestURI()).willReturn("/actuator/health");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("일반 API는 필터링함")
        void givenProtectedApiRequest_whenShouldNotFilter_thenReturnFalse() {
            // Given
            given(request.getRequestURI()).willReturn("/api/v1/profiles");
            given(request.getMethod()).willReturn("GET");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("계정 API의 GET 요청은 필터링함")
        void givenAccountGetRequest_whenShouldNotFilter_thenReturnFalse() {
            // Given
            given(request.getRequestURI()).willReturn("/api/v1/accounts");
            given(request.getMethod()).willReturn("GET");

            // When
            boolean result = jwtAuthenticationFilter.shouldNotFilter(request);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("extractTokenFromHeader 메서드 테스트")
    class ExtractTokenFromHeaderTest {

        @Test
        @DisplayName("올바른 Bearer 토큰에서 토큰 추출")
        void givenValidBearerToken_whenExtractToken_thenReturnToken() throws Exception {
            // Given
            given(request.getHeader("Authorization")).willReturn(BEARER_TOKEN);

            // When
            String result = invokePrivateMethod("extractTokenFromHeader", request);

            // Then
            assertThat(result).isEqualTo(VALID_TOKEN);
        }

        @Test
        @DisplayName("Authorization 헤더가 null일 때 null 반환")
        void givenNullAuthorizationHeader_whenExtractToken_thenReturnNull() throws Exception {
            // Given
            given(request.getHeader("Authorization")).willReturn(null);

            // When
            String result = invokePrivateMethod("extractTokenFromHeader", request);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Bearer 접두사가 없는 토큰일 때 null 반환")
        void givenTokenWithoutBearerPrefix_whenExtractToken_thenReturnNull() throws Exception {
            // Given
            given(request.getHeader("Authorization")).willReturn(VALID_TOKEN);

            // When
            String result = invokePrivateMethod("extractTokenFromHeader", request);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("빈 문자열 Authorization 헤더일 때 null 반환")
        void givenEmptyAuthorizationHeader_whenExtractToken_thenReturnNull() throws Exception {
            // Given
            given(request.getHeader("Authorization")).willReturn("");

            // When
            String result = invokePrivateMethod("extractTokenFromHeader", request);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Bearer만 있고 토큰이 없을 때 빈 문자열 반환")
        void givenBearerWithoutToken_whenExtractToken_thenReturnEmptyString() throws Exception {
            // Given
            given(request.getHeader("Authorization")).willReturn("Bearer ");

            // When
            String result = invokePrivateMethod("extractTokenFromHeader", request);

            // Then
            assertThat(result).isEmpty();
        }
    }

    // 프라이빗 메서드 테스트를 위한 헬퍼 메서드
    @SuppressWarnings("unchecked")
    private <T> T invokePrivateMethod(String methodName, Object... args) throws Exception {
        var method = JwtAuthenticationFilter.class.getDeclaredMethod(methodName, HttpServletRequest.class);
        method.setAccessible(true);
        return (T) method.invoke(jwtAuthenticationFilter, args);
    }
}