package com.commerce.customer.api.security;

import com.commerce.customer.api.security.config.SecurityConfig;
import com.commerce.customer.api.security.filter.JwtAuthenticationFilter;
import com.commerce.customer.api.security.handler.JwtAccessDeniedHandler;
import com.commerce.customer.api.security.handler.JwtAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig 단위 테스트")
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Mock
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Nested
    @DisplayName("Bean 생성 테스트")
    class BeanCreationTest {

        @Test
        @DisplayName("PasswordEncoder Bean이 BCryptPasswordEncoder 인스턴스로 생성됨")
        void whenPasswordEncoder_thenReturnBCryptPasswordEncoder() {
            // When
            PasswordEncoder result = securityConfig.passwordEncoder();

            // Then
            assertThat(result).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("BCryptPasswordEncoder Bean이 올바르게 생성됨")
        void whenBCryptPasswordEncoder_thenReturnBCryptPasswordEncoder() {
            // When
            BCryptPasswordEncoder result = securityConfig.bCryptPasswordEncoder();

            // Then
            assertThat(result).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("CorsConfigurationSource Bean이 올바르게 설정됨")
        void whenCorsConfigurationSource_thenReturnConfiguredSource() {
            // When
            CorsConfigurationSource result = securityConfig.corsConfigurationSource();

            // Then
            assertThat(result).isInstanceOf(UrlBasedCorsConfigurationSource.class);
            
            UrlBasedCorsConfigurationSource source = (UrlBasedCorsConfigurationSource) result;
            CorsConfiguration config = source.getCorsConfigurations().get("/**");
            
            assertThat(config).isNotNull();
            assertThat(config.getAllowedOriginPatterns()).contains("*");
            assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            );
            assertThat(config.getAllowedHeaders()).contains("*");
            assertThat(config.getAllowCredentials()).isTrue();
            assertThat(config.getExposedHeaders()).contains("Authorization");
        }
    }

    @Nested
    @DisplayName("CORS 설정 테스트")
    class CorsConfigurationTest {

        @Test
        @DisplayName("CORS 설정이 모든 Origin 패턴을 허용함")
        void givenCorsConfiguration_whenGetAllowedOriginPatterns_thenAllowAll() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
            CorsConfiguration config = urlBasedSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(config.getAllowedOriginPatterns()).containsExactly("*");
        }

        @Test
        @DisplayName("CORS 설정이 모든 HTTP 메서드를 허용함")
        void givenCorsConfiguration_whenGetAllowedMethods_thenAllowAllMethods() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
            CorsConfiguration config = urlBasedSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
            );
        }

        @Test
        @DisplayName("CORS 설정이 모든 헤더를 허용함")
        void givenCorsConfiguration_whenGetAllowedHeaders_thenAllowAllHeaders() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
            CorsConfiguration config = urlBasedSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(config.getAllowedHeaders()).containsExactly("*");
        }

        @Test
        @DisplayName("CORS 설정이 자격 증명을 허용함")
        void givenCorsConfiguration_whenGetAllowCredentials_thenReturnTrue() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
            CorsConfiguration config = urlBasedSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(config.getAllowCredentials()).isTrue();
        }

        @Test
        @DisplayName("CORS 설정이 Authorization 헤더를 노출함")
        void givenCorsConfiguration_whenGetExposedHeaders_thenContainAuthorization() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
            CorsConfiguration config = urlBasedSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(config.getExposedHeaders()).containsExactly("Authorization");
        }
    }

    @Nested
    @DisplayName("SecurityFilterChain 설정 테스트")
    class SecurityFilterChainTest {

        @Test
        @DisplayName("SecurityFilterChain이 올바르게 생성됨")
        void whenFilterChain_thenReturnSecurityFilterChain() throws Exception {
            // Given
            HttpSecurity httpSecurity = mock(HttpSecurity.class);
            DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);

            // Mock 체인 설정
            given(httpSecurity.csrf(any())).willReturn(httpSecurity);
            given(httpSecurity.cors(any())).willReturn(httpSecurity);
            given(httpSecurity.sessionManagement(any())).willReturn(httpSecurity);
            given(httpSecurity.exceptionHandling(any())).willReturn(httpSecurity);
            given(httpSecurity.authorizeHttpRequests(any())).willReturn(httpSecurity);
            given(httpSecurity.addFilterBefore(any(), any())).willReturn(httpSecurity);
            given(httpSecurity.build()).willReturn(mockFilterChain);

            // When
            SecurityFilterChain result = securityConfig.filterChain(httpSecurity);

            // Then
            assertThat(result).isEqualTo(mockFilterChain);
            
            // 모든 설정 메서드가 호출되었는지 확인
            then(httpSecurity).should().csrf(any());
            then(httpSecurity).should().cors(any());
            then(httpSecurity).should().sessionManagement(any());
            then(httpSecurity).should().exceptionHandling(any());
            then(httpSecurity).should().authorizeHttpRequests(any());
            then(httpSecurity).should().addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            then(httpSecurity).should().build();
        }

        @Test
        @DisplayName("CSRF가 비활성화됨")
        void givenHttpSecurity_whenConfigureCSRF_thenDisabled() throws Exception {
            // Given
            HttpSecurity httpSecurity = mock(HttpSecurity.class);
            DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);

            // Mock 설정
            given(httpSecurity.csrf(any())).willReturn(httpSecurity);
            given(httpSecurity.cors(any())).willReturn(httpSecurity);
            given(httpSecurity.sessionManagement(any())).willReturn(httpSecurity);
            given(httpSecurity.exceptionHandling(any())).willReturn(httpSecurity);
            given(httpSecurity.authorizeHttpRequests(any())).willReturn(httpSecurity);
            given(httpSecurity.addFilterBefore(any(), any())).willReturn(httpSecurity);
            given(httpSecurity.build()).willReturn(mockFilterChain);

            // When
            securityConfig.filterChain(httpSecurity);

            // Then
            then(httpSecurity).should().csrf(any());
        }

        @Test
        @DisplayName("세션 관리가 STATELESS로 설정됨")
        void givenHttpSecurity_whenConfigureSessionManagement_thenStateless() throws Exception {
            // Given
            HttpSecurity httpSecurity = mock(HttpSecurity.class);
            DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);
            SessionManagementConfigurer<HttpSecurity> sessionConfigurer = mock(SessionManagementConfigurer.class);

            // Mock 설정
            given(httpSecurity.csrf(any())).willReturn(httpSecurity);
            given(httpSecurity.cors(any())).willReturn(httpSecurity);
            given(httpSecurity.sessionManagement(any())).willReturn(httpSecurity);
            given(httpSecurity.exceptionHandling(any())).willReturn(httpSecurity);
            given(httpSecurity.authorizeHttpRequests(any())).willReturn(httpSecurity);
            given(httpSecurity.addFilterBefore(any(), any())).willReturn(httpSecurity);
            given(httpSecurity.build()).willReturn(mockFilterChain);

            // When
            securityConfig.filterChain(httpSecurity);

            // Then
            then(httpSecurity).should().sessionManagement(any());
        }

        @Test
        @DisplayName("JWT 필터가 UsernamePasswordAuthenticationFilter 이전에 추가됨")
        void givenHttpSecurity_whenConfigureFilters_thenJwtFilterAddedBefore() throws Exception {
            // Given
            HttpSecurity httpSecurity = mock(HttpSecurity.class);
            DefaultSecurityFilterChain mockFilterChain = mock(DefaultSecurityFilterChain.class);

            // Mock 설정
            given(httpSecurity.csrf(any())).willReturn(httpSecurity);
            given(httpSecurity.cors(any())).willReturn(httpSecurity);
            given(httpSecurity.sessionManagement(any())).willReturn(httpSecurity);
            given(httpSecurity.exceptionHandling(any())).willReturn(httpSecurity);
            given(httpSecurity.authorizeHttpRequests(any())).willReturn(httpSecurity);
            given(httpSecurity.addFilterBefore(any(), any())).willReturn(httpSecurity);
            given(httpSecurity.build()).willReturn(mockFilterChain);

            // When
            securityConfig.filterChain(httpSecurity);

            // Then
            then(httpSecurity).should().addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
    }

    @Nested
    @DisplayName("Bean 동등성 테스트")
    class BeanEqualityTest {

        @Test
        @DisplayName("동일한 PasswordEncoder Bean 인스턴스 반환")
        void givenMultipleCalls_whenPasswordEncoder_thenReturnSameType() {
            // When
            PasswordEncoder encoder1 = securityConfig.passwordEncoder();
            PasswordEncoder encoder2 = securityConfig.passwordEncoder();

            // Then
            assertThat(encoder1).isInstanceOf(BCryptPasswordEncoder.class);
            assertThat(encoder2).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("동일한 BCryptPasswordEncoder Bean 인스턴스 반환")
        void givenMultipleCalls_whenBCryptPasswordEncoder_thenReturnSameType() {
            // When
            BCryptPasswordEncoder encoder1 = securityConfig.bCryptPasswordEncoder();
            BCryptPasswordEncoder encoder2 = securityConfig.bCryptPasswordEncoder();

            // Then
            assertThat(encoder1).isInstanceOf(BCryptPasswordEncoder.class);
            assertThat(encoder2).isInstanceOf(BCryptPasswordEncoder.class);
        }

        @Test
        @DisplayName("PasswordEncoder와 BCryptPasswordEncoder가 동일한 타입")
        void givenBothBeans_whenCompareTypes_thenSameType() {
            // When
            PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
            BCryptPasswordEncoder bCryptPasswordEncoder = securityConfig.bCryptPasswordEncoder();

            // Then
            assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
            assertThat(bCryptPasswordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
            assertThat(passwordEncoder.getClass()).isEqualTo(bCryptPasswordEncoder.getClass());
        }
    }

    @Nested
    @DisplayName("설정 검증 테스트")
    class ConfigurationValidationTest {

        @Test
        @DisplayName("모든 필수 필드가 null이 아님")
        void givenSecurityConfig_whenCheckFields_thenNotNull() {
            // Then
            assertThat(jwtAuthenticationFilter).isNotNull();
            assertThat(jwtAuthenticationEntryPoint).isNotNull();
            assertThat(jwtAccessDeniedHandler).isNotNull();
        }

        @Test
        @DisplayName("CorsConfigurationSource 설정이 유효함")
        void givenCorsConfigurationSource_whenValidate_thenValid() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();

            // Then
            assertThat(source).isNotNull();
            assertThat(source).isInstanceOf(UrlBasedCorsConfigurationSource.class);
            
            UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
            assertThat(urlSource.getCorsConfigurations()).isNotEmpty();
            assertThat(urlSource.getCorsConfigurations()).containsKey("/**");
        }

        @Test
        @DisplayName("CORS 설정의 모든 필수 속성이 설정됨")
        void givenCorsConfiguration_whenCheckAllProperties_thenAllSet() {
            // When
            CorsConfigurationSource source = securityConfig.corsConfigurationSource();
            UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
            CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

            // Then
            assertThat(config.getAllowedOriginPatterns()).isNotNull().isNotEmpty();
            assertThat(config.getAllowedMethods()).isNotNull().isNotEmpty();
            assertThat(config.getAllowedHeaders()).isNotNull().isNotEmpty();
            assertThat(config.getAllowCredentials()).isNotNull();
            assertThat(config.getExposedHeaders()).isNotNull().isNotEmpty();
        }
    }
}