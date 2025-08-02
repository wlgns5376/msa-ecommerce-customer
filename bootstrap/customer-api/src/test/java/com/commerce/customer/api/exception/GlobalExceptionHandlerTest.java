package com.commerce.customer.api.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("GlobalExceptionHandler 단위 테스트")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private TestController testController;

    @BeforeEach
    void setUp() {
        testController = new TestController();
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(testController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("유효성 검증 예외 처리 테스트")
    class ValidationExceptionHandlingTest {

        @Test
        @DisplayName("MethodArgumentNotValidException 발생시 BAD_REQUEST와 필드별 에러 메시지를 반환해야 한다")
        void shouldHandleMethodArgumentNotValidException() throws Exception {
            // Given
            TestRequest invalidRequest = new TestRequest("", "invalid-email");
            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.email").exists());
        }

        @Test
        @DisplayName("여러 필드 유효성 검증 실패시 모든 필드 에러를 반환해야 한다")
        void shouldHandleMultipleValidationErrors() throws Exception {
            // Given
            TestRequest invalidRequest = new TestRequest(null, null);
            String requestBody = objectMapper.writeValueAsString(invalidRequest);

            // When & Then
            mockMvc.perform(post("/test/validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").exists())
                    .andExpect(jsonPath("$.email").exists());
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException 처리 테스트")
    class IllegalArgumentExceptionHandlingTest {

        @Test
        @DisplayName("IllegalArgumentException 발생시 BAD_REQUEST와 ErrorResponse를 반환해야 한다")
        void shouldHandleIllegalArgumentException() throws Exception {
            // Given
            String errorMessage = "잘못된 인수입니다";

            // When & Then
            mockMvc.perform(get("/test/illegal-argument")
                            .param("message", errorMessage))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("INVALID_ARGUMENT")))
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("빈 메시지로 IllegalArgumentException 발생시 빈 메시지를 반환해야 한다")
        void shouldHandleIllegalArgumentExceptionWithEmptyMessage() throws Exception {
            // Given
            String emptyMessage = "";

            // When & Then
            mockMvc.perform(get("/test/illegal-argument")
                            .param("message", emptyMessage))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("INVALID_ARGUMENT")))
                    .andExpect(jsonPath("$.message", is(emptyMessage)));
        }
    }

    @Nested
    @DisplayName("IllegalStateException 처리 테스트")
    class IllegalStateExceptionHandlingTest {

        @Test
        @DisplayName("IllegalStateException 발생시 BAD_REQUEST와 ErrorResponse를 반환해야 한다")
        void shouldHandleIllegalStateException() throws Exception {
            // Given
            String errorMessage = "잘못된 상태입니다";

            // When & Then
            mockMvc.perform(get("/test/illegal-state")
                            .param("message", errorMessage))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("INVALID_STATE")))
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("null 메시지로 IllegalStateException 발생시 null을 반환해야 한다")
        void shouldHandleIllegalStateExceptionWithNullMessage() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/illegal-state"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("INVALID_STATE")))
                    .andExpect(jsonPath("$.message").doesNotExist());
        }
    }

    @Nested
    @DisplayName("ResourceNotFoundException 처리 테스트")
    class ResourceNotFoundExceptionHandlingTest {

        @Test
        @DisplayName("ResourceNotFoundException 발생시 NOT_FOUND와 ErrorResponse를 반환해야 한다")
        void shouldHandleResourceNotFoundException() throws Exception {
            // Given
            String errorMessage = "리소스를 찾을 수 없습니다";

            // When & Then
            mockMvc.perform(get("/test/resource-not-found")
                            .param("message", errorMessage))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                    .andExpect(jsonPath("$.message", is(errorMessage)));
        }

        @Test
        @DisplayName("상세한 리소스 정보가 포함된 ResourceNotFoundException 처리 테스트")
        void shouldHandleResourceNotFoundExceptionWithDetails() throws Exception {
            // Given
            String detailedMessage = "사용자 ID 123을 찾을 수 없습니다";

            // When & Then
            mockMvc.perform(get("/test/resource-not-found")
                            .param("message", detailedMessage))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("RESOURCE_NOT_FOUND")))
                    .andExpect(jsonPath("$.message", is(detailedMessage)));
        }
    }

    @Nested
    @DisplayName("일반 Exception 처리 테스트")
    class GenericExceptionHandlingTest {

        @Test
        @DisplayName("예상치 못한 Exception 발생시 INTERNAL_SERVER_ERROR와 일반적인 메시지를 반환해야 한다")
        void shouldHandleGenericException() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/generic-exception"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")))
                    .andExpect(jsonPath("$.message", is("서버에서 오류가 발생했습니다.")));
        }

        @Test
        @DisplayName("RuntimeException 발생시 INTERNAL_SERVER_ERROR를 반환해야 한다")
        void shouldHandleRuntimeException() throws Exception {
            // When & Then
            mockMvc.perform(get("/test/runtime-exception"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.code", is("INTERNAL_SERVER_ERROR")))
                    .andExpect(jsonPath("$.message", is("서버에서 오류가 발생했습니다.")));
        }
    }

    @Nested
    @DisplayName("ErrorResponse 내부 클래스 테스트")
    class ErrorResponseTest {

        @Test
        @DisplayName("ErrorResponse 생성시 code와 message가 올바르게 설정되어야 한다")
        void shouldCreateErrorResponseWithCodeAndMessage() {
            // Given
            String code = "TEST_CODE";
            String message = "테스트 메시지";

            // When
            GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(code, message);

            // Then
            assertThat(errorResponse.getCode()).isEqualTo(code);
            assertThat(errorResponse.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("ErrorResponse에 null 값이 들어가도 정상 동작해야 한다")
        void shouldCreateErrorResponseWithNullValues() {
            // Given
            String nullCode = null;
            String nullMessage = null;

            // When
            GlobalExceptionHandler.ErrorResponse errorResponse = 
                new GlobalExceptionHandler.ErrorResponse(nullCode, nullMessage);

            // Then
            assertThat(errorResponse.getCode()).isNull();
            assertThat(errorResponse.getMessage()).isNull();
        }
    }

    // 테스트용 컨트롤러
    @RestController
    static class TestController {

        @PostMapping("/test/validation")
        public String testValidation(@Valid @RequestBody TestRequest request) {
            return "success";
        }

        @GetMapping("/test/illegal-argument")
        public String testIllegalArgument(String message) {
            throw new IllegalArgumentException(message);
        }

        @GetMapping("/test/illegal-state")
        public String testIllegalState(String message) {
            throw new IllegalStateException(message);
        }

        @GetMapping("/test/resource-not-found")
        public String testResourceNotFound(String message) {
            throw new ResourceNotFoundException(message);
        }

        @GetMapping("/test/generic-exception")
        public String testGenericException() {
            throw new RuntimeException("예상치 못한 오류");
        }

        @GetMapping("/test/runtime-exception")
        public String testRuntimeException() {
            throw new RuntimeException("런타임 오류");
        }
    }

    // 테스트용 요청 DTO
    static class TestRequest {
        @NotBlank(message = "이름은 필수입니다")
        private String name;

        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        private String email;

        public TestRequest() {}

        public TestRequest(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}