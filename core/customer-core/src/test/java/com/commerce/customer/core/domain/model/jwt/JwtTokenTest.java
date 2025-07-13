package com.commerce.customer.core.domain.model.jwt;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtToken 값 객체 테스트")
class JwtTokenTest {

    @Nested
    @DisplayName("JWT 토큰 생성 테스트")
    class CreateTokenTest {

        @Test
        @DisplayName("유효한 정보로 JWT 토큰을 생성할 수 있다")
        void createToken_WithValidData_ShouldSuccess() {
            // Given
            String tokenValue = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0In0.test";
            JwtTokenType tokenType = JwtTokenType.ACCESS;
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusMinutes(15);

            // When
            JwtToken token = JwtToken.of(tokenValue, tokenType, issuedAt, expiresAt);

            // Then
            assertThat(token.getValue()).isEqualTo(tokenValue);
            assertThat(token.getType()).isEqualTo(tokenType);
            assertThat(token.getIssuedAt()).isEqualTo(issuedAt);
            assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
        }

        @Test
        @DisplayName("null 값으로 토큰 생성 시 예외가 발생한다")
        void createToken_WithNullValues_ShouldThrowException() {
            // Given
            String tokenValue = "test.token.value";
            JwtTokenType tokenType = JwtTokenType.ACCESS;
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusMinutes(15);

            // When & Then
            assertThatThrownBy(() -> JwtToken.of(null, tokenType, issuedAt, expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT 토큰 값은 필수입니다");

            assertThatThrownBy(() -> JwtToken.of(tokenValue, null, issuedAt, expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("JWT 토큰 타입은 필수입니다");

            assertThatThrownBy(() -> JwtToken.of(tokenValue, tokenType, null, expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("발급 시간은 필수입니다");

            assertThatThrownBy(() -> JwtToken.of(tokenValue, tokenType, issuedAt, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료 시간은 필수입니다");
        }

        @Test
        @DisplayName("만료 시간이 발급 시간보다 이전이면 예외가 발생한다")
        void createToken_WithExpiresBeforeIssued_ShouldThrowException() {
            // Given
            String tokenValue = "test.token.value";
            JwtTokenType tokenType = JwtTokenType.ACCESS;
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.minusMinutes(1); // 과거 시간

            // When & Then
            assertThatThrownBy(() -> JwtToken.of(tokenValue, tokenType, issuedAt, expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("만료 시간은 발급 시간보다 이후여야 합니다");
        }
    }

    @Nested
    @DisplayName("토큰 만료 검증 테스트")
    class TokenExpirationTest {

        @Test
        @DisplayName("만료되지 않은 토큰은 유효하다")
        void isValid_WithNotExpiredToken_ShouldReturnTrue() {
            // Given
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusMinutes(15);
            JwtToken token = JwtToken.of("test.token", JwtTokenType.ACCESS, issuedAt, expiresAt);

            // When & Then
            assertThat(token.isValid()).isTrue();
            assertThat(token.isExpired()).isFalse();
        }

        @Test
        @DisplayName("만료된 토큰은 유효하지 않다")
        void isValid_WithExpiredToken_ShouldReturnFalse() {
            // Given
            LocalDateTime issuedAt = LocalDateTime.now().minusMinutes(30);
            LocalDateTime expiresAt = issuedAt.plusMinutes(15); // 이미 만료됨
            JwtToken token = JwtToken.of("test.token", JwtTokenType.ACCESS, issuedAt, expiresAt);

            // When & Then
            assertThat(token.isValid()).isFalse();
            assertThat(token.isExpired()).isTrue();
        }
    }

    @Nested
    @DisplayName("토큰 동등성 테스트")
    class TokenEqualityTest {

        @Test
        @DisplayName("같은 토큰 값을 가진 토큰은 동일하다")
        void equals_WithSameTokenValue_ShouldReturnTrue() {
            // Given
            String tokenValue = "test.token.value";
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusMinutes(15);

            JwtToken token1 = JwtToken.of(tokenValue, JwtTokenType.ACCESS, issuedAt, expiresAt);
            JwtToken token2 = JwtToken.of(tokenValue, JwtTokenType.REFRESH, issuedAt, expiresAt);

            // When & Then
            assertThat(token1).isEqualTo(token2);
            assertThat(token1.hashCode()).isEqualTo(token2.hashCode());
        }

        @Test
        @DisplayName("다른 토큰 값을 가진 토큰은 동일하지 않다")
        void equals_WithDifferentTokenValue_ShouldReturnFalse() {
            // Given
            LocalDateTime issuedAt = LocalDateTime.now();
            LocalDateTime expiresAt = issuedAt.plusMinutes(15);

            JwtToken token1 = JwtToken.of("token1", JwtTokenType.ACCESS, issuedAt, expiresAt);
            JwtToken token2 = JwtToken.of("token2", JwtTokenType.ACCESS, issuedAt, expiresAt);

            // When & Then
            assertThat(token1).isNotEqualTo(token2);
        }
    }
}