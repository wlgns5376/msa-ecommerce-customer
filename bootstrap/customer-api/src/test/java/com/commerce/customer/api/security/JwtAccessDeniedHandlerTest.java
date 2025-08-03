package com.commerce.customer.api.security;

import com.commerce.customer.api.security.handler.JwtAccessDeniedHandler;
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
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAccessDeniedHandler 단위 테스트")
class JwtAccessDeniedHandlerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter printWriter;

    @InjectMocks
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    private static final String REQUEST_URI = "/api/v1/profiles";
    private static final String ACCESS_DENIED_MESSAGE = "권한이 없습니다";

    @BeforeEach
    void setUp() throws IOException {
        given(response.getWriter()).willReturn(printWriter);
    }

    @Nested
    @DisplayName("handle 메서드 테스트")
    class HandleTest {

        @Test
        @DisplayName("접근 거부 시 403 응답과 에러 메시지 반환")
        void givenAccessDeniedException_whenHandle_thenReturn403WithErrorMessage() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            String expectedJson = "{\"error\":\"access_denied\"}";
            
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(any(Map.class))).willReturn(expectedJson);

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            // Then
            then(response).should().setStatus(HttpServletResponse.SC_FORBIDDEN);
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding("UTF-8");
            then(printWriter).should().write(expectedJson);
        }

        @Test
        @DisplayName("에러 응답 JSON 구조 검증")
        void givenAccessDeniedException_whenHandle_thenErrorResponseHasCorrectStructure() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            
            assertThat(errorResponse).containsKey("timestamp");
            assertThat(errorResponse.get("status")).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
            assertThat(errorResponse.get("error")).isEqualTo("Forbidden");
            assertThat(errorResponse.get("message")).isEqualTo("접근 권한이 없습니다.");
            assertThat(errorResponse.get("path")).isEqualTo(REQUEST_URI);
        }

        @Test
        @DisplayName("타임스탬프가 올바른 형식으로 생성됨")
        void givenAccessDeniedException_whenHandle_thenTimestampIsValidFormat() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            LocalDateTime beforeHandle = LocalDateTime.now();

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            LocalDateTime afterHandle = LocalDateTime.now();

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            String timestamp = (String) errorResponse.get("timestamp");
            
            assertThat(timestamp).isNotNull();
            
            // 타임스탬프 파싱 검증
            LocalDateTime parsedTimestamp = LocalDateTime.parse(timestamp);
            assertThat(parsedTimestamp).isBetween(beforeHandle.minusSeconds(1), afterHandle.plusSeconds(1));
        }

        @Test
        @DisplayName("다양한 요청 URI에 대해 올바른 경로 반환")
        void givenDifferentRequestUri_whenHandle_thenCorrectPathInResponse() throws IOException {
            // Given
            String[] testUris = {
                "/api/v1/accounts",
                "/api/v1/profiles/123",
                "/api/v1/profiles/123/addresses",
                "/admin/users"
            };

            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);

            for (String uri : testUris) {
                // Given
                given(request.getRequestURI()).willReturn(uri);

                ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
                given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

                // When
                jwtAccessDeniedHandler.handle(request, response, exception);

                // Then
                Map<String, Object> errorResponse = mapCaptor.getValue();
                assertThat(errorResponse.get("path")).isEqualTo(uri);
            }
        }

        @Test
        @DisplayName("null 예외 메시지 처리")
        void givenNullExceptionMessage_whenHandle_thenHandleGracefully() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(null);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("message")).isEqualTo("접근 권한이 없습니다.");
        }

        @Test
        @DisplayName("빈 요청 URI 처리")
        void givenEmptyRequestUri_whenHandle_thenHandleGracefully() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn("");

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("path")).isEqualTo("");
        }

        @Test
        @DisplayName("ObjectMapper 직렬화 예외 시 처리")
        void givenObjectMapperThrowsException_whenHandle_thenExceptionPropagated() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(any(Map.class))).willThrow(new RuntimeException("JSON 직렬화 실패"));

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                jwtAccessDeniedHandler.handle(request, response, exception);
            });
        }

        @Test
        @DisplayName("PrintWriter가 null일 때 예외 처리")
        void givenNullPrintWriter_whenHandle_thenExceptionThrown() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(response.getWriter()).willReturn(null);

            // When & Then
            org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
                jwtAccessDeniedHandler.handle(request, response, exception);
            });
        }
    }

    @Nested
    @DisplayName("응답 헤더 설정 테스트")
    class ResponseHeaderTest {

        @Test
        @DisplayName("응답 헤더가 올바르게 설정됨")
        void givenAccessDeniedException_whenHandle_thenResponseHeadersSetCorrectly() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);
            given(objectMapper.writeValueAsString(any(Map.class))).willReturn("{}");

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            // Then
            then(response).should().setStatus(HttpServletResponse.SC_FORBIDDEN);
            then(response).should().setContentType(MediaType.APPLICATION_JSON_VALUE);
            then(response).should().setCharacterEncoding("UTF-8");
        }

        @Test
        @DisplayName("상태 코드가 403으로 설정됨")
        void givenAccessDeniedException_whenHandle_thenStatusIs403() throws IOException {
            // Given
            AccessDeniedException exception = new AccessDeniedException(ACCESS_DENIED_MESSAGE);
            given(request.getRequestURI()).willReturn(REQUEST_URI);

            ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
            given(objectMapper.writeValueAsString(mapCaptor.capture())).willReturn("{}");

            // When
            jwtAccessDeniedHandler.handle(request, response, exception);

            // Then
            Map<String, Object> errorResponse = mapCaptor.getValue();
            assertThat(errorResponse.get("status")).isEqualTo(403);
        }
    }
}