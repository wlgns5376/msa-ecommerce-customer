package com.commerce.customer.api.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ResourceNotFoundException 단위 테스트")
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("메시지로 예외 생성시 메시지가 올바르게 설정되어야 한다")
    void shouldCreateExceptionWithMessage() {
        // Given
        String expectedMessage = "리소스를 찾을 수 없습니다";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(expectedMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("RuntimeException을 상속받아야 한다")
    void shouldExtendRuntimeException() {
        // Given
        String message = "테스트 메시지";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(message);

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("null 메시지로 예외 생성시 null이 반환되어야 한다")
    void shouldCreateExceptionWithNullMessage() {
        // Given
        String nullMessage = null;

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(nullMessage);

        // Then
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("빈 문자열 메시지로 예외 생성시 빈 문자열이 반환되어야 한다")
    void shouldCreateExceptionWithEmptyMessage() {
        // Given
        String emptyMessage = "";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(emptyMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo("");
    }

    @Test
    @DisplayName("공백 문자열 메시지로 예외 생성시 공백이 포함된 문자열이 반환되어야 한다")
    void shouldCreateExceptionWithWhitespaceMessage() {
        // Given
        String whitespaceMessage = "   ";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(whitespaceMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo("   ");
    }

    @Test
    @DisplayName("긴 메시지로 예외 생성시 전체 메시지가 올바르게 설정되어야 한다")
    void shouldCreateExceptionWithLongMessage() {
        // Given
        String longMessage = "이것은 매우 긴 메시지입니다. ".repeat(10);

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(longMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("특수 문자가 포함된 메시지로 예외 생성시 특수 문자가 올바르게 설정되어야 한다")
    void shouldCreateExceptionWithSpecialCharactersMessage() {
        // Given
        String specialMessage = "리소스ID: #123@domain.com을 찾을 수 없습니다 (상태: 404)";

        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(specialMessage);

        // Then
        assertThat(exception.getMessage()).isEqualTo(specialMessage);
    }
}