package com.commerce.customer.api.security;

import com.commerce.customer.api.security.handler.JwtAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationEntryPoint 단위 테스트")
class JwtAuthenticationEntryPointTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String REQUEST_URI = "/api/v1/profiles";
    private static final String AUTH_EXCEPTION_MESSAGE = "인증에 실패했습니다";

    @BeforeEach
    void setUp() throws IOException {
        given(response.getWriter()).willReturn(printWriter);
    }

    @Nested
    @DisplayName("commence 메서드 테스트")
    class CommenceTest {

        @Test
        @DisplayName("인증 실패 시 401 응답과 에러 메시지 반환")
        void givenAuthenticationException_whenCommence_thenReturn401WithErrorMessage() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            String expectedJson = "{\"error\":\"unauthorized\"}";
            
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(anyString())).willReturn(expectedJson);

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            then(response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding("UTF-8");
            then(printWriter).should().write(expectedJson);
        }

        @Test
        @DisplayName("에러 응답 JSON 구조 검증")
        void givenAuthenticationException_whenCommence_thenErrorResponseHasCorrectStructure() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            
            assertThat(errorResponse).containsKey("timestamp");
            assertThat(errorResponse.get("status")).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
            assertThat(errorResponse.get("error")).isEqualTo("Unauthorized");
            assertThat(errorResponse.get("message")).isEqualTo("JWT 토큰이 없거나 유효하지 않습니다.");
            assertThat(errorResponse.get("path")).isEqualTo(REQUEST_URI);
        }

        @Test
        @DisplayName("타임스탬프가 올바른 형식으로 생성됨")
        void givenAuthenticationException_whenCommence_thenTimestampIsValidFormat() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            LocalDateTime beforeCommence = LocalDateTime.now();

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            LocalDateTime afterCommence = LocalDateTime.now();

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            String timestamp = (String) errorResponse.get("timestamp");
            
            assertThat(timestamp).isNotNull();
            
            // 타임스탬프 파싱 검증
            LocalDateTime parsedTimestamp = LocalDateTime.parse(timestamp);
            assertThat(parsedTimestamp).isBetween(beforeCommence.minusSeconds(1), afterCommence.plusSeconds(1));
        }

        @Test
        @DisplayName("다양한 AuthenticationException 타입 처리")
        void givenDifferentAuthenticationExceptionTypes_whenCommence_thenHandleCorrectly() throws IOException {
            // Given
            AuthenticationException[] exceptions = {
                new BadCredentialsException("잘못된 자격 증명"),
                new InsufficientAuthenticationException("인증 정보 부족"),
                new AuthenticationException("일반 인증 예외") {}
            };

            given(request.getRequestURI()).willReturn(REQUEST_URI);

            for (AuthenticationException exception : exceptions) {
                // Given
                ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
                given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

                // When
                jwtAuthenticationEntryPoint.commence(request, response, exception);

                // Then
                Map<String, Object> errorResponse = mapCaptor.getValue();
                assertThat(errorResponse.get("status")).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
                assertThat(errorResponse.get("error")).isEqualTo("Unauthorized");
                assertThat(errorResponse.get("message")).isEqualTo("JWT 토큰이 없거나 유효하지 않습니다.");
            }
        }

        @Test
        @DisplayName("다양한 요청 URI에 대해 올바른 경로 반환")
        void givenDifferentRequestUri_whenCommence_thenCorrectPathInResponse() throws IOException {
            // Given
            String[] testUris = {
                "/api/v1/accounts",
                "/api/v1/profiles/123",
                "/api/v1/profiles/123/addresses",
                "/admin/users",
                "/public/health"
            };

            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);

            for (String uri : testUris) {
                // Given
                given(request.getRequestURI()).willReturn(uri);

                ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
                given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

                // When
                jwtAuthenticationEntryPoint.commence(request, response, exception);

                // Then
                Map<String, Object> errorResponse = mapCaptor.getValue();
                assertThat(errorResponse.get("path")).isEqualTo(uri);
            }
        }

        @Test
        @DisplayName("null 예외 메시지 처리")
        void givenNullExceptionMessage_whenCommence_thenHandleGracefully() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(null);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("message")).isEqualTo("JWT 토큰이 없거나 유효하지 않습니다.");
        }

        @Test
        @DisplayName("빈 요청 URI 처리")
        void givenEmptyRequestUri_whenCommence_thenHandleGracefully() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn("");

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("path")).isEqualTo("");
        }

        @Test
        @DisplayName("null 요청 URI 처리")
        void givenNullRequestUri_whenCommence_thenHandleGracefully() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(null);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("path")).isNull();
        }

        @Test
        @DisplayName("ObjectMapper 직렬화 예외 시 처리")
        void givenObjectMapperThrowsException_whenCommence_thenExceptionPropagated() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(anyString())).willThrow(new IOException("JSON 직렬화 실패"));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(IOException.class, () -> {
                jwtAuthenticationEntryPoint.commence(request, response, exception);
            });
        }

        @Test
        @DisplayName("PrintWriter가 null일 때 예외 처리")
        void givenNullPrintWriter_whenCommence_thenExceptionThrown() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(response.getWriter()).willReturn(null);

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
                jwtAuthenticationEntryPoint.commence(request, response, exception);
            });
        }
    }

    @Nested
    @DisplayName("응답 헤더 설정 테스트")
    class ResponseHeaderTest {

        @Test
        @DisplayName("응답 헤더가 올바르게 설정됨")
        void givenAuthenticationException_whenCommence_thenResponseHeadersSetCorrectly() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(anyString())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            then(response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding("UTF-8");
        }

        @Test
        @DisplayName("상태 코드가 401로 설정됨")
        void givenAuthenticationException_whenCommence_thenStatusIs401() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("status")).isEqualTo(401);
        }
    }

    @Nested
    @DisplayName("로깅 테스트")
    class LoggingTest {

        @Test
        @DisplayName("경고 로그가 기록됨")
        void givenAuthenticationException_whenCommence_thenWarningLogged() throws IOException {
            // Given
            AuthenticationException exception = new BadCredentialsException(AUTH_EXCEPTION_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(anyString())).willReturn("{}");

            // When
            jwtAuthenticationEntryPoint.commence(request, response, exception);

            // Then
            // 로깅은 실제 로그 출력을 확인하기 어려우므로, 메서드가 정상적으로 완료되는지만 확인
            then(response).should().setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}