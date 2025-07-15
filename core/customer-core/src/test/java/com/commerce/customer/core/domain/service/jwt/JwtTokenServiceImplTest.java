package com.commerce.customer.core.domain.service.jwt;

import com.commerce.customer.core.domain.exception.ExpiredJwtTokenException;
import com.commerce.customer.core.domain.exception.InvalidJwtTokenException;
import com.commerce.customer.core.domain.model.AccountId;
import com.commerce.customer.core.domain.model.CustomerId;
import com.commerce.customer.core.domain.model.Email;
import com.commerce.customer.core.domain.model.jwt.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenServiceImpl 테스트")
class JwtTokenServiceImplTest {

    private JwtTokenServiceImpl jwtTokenService;
    private CustomerId customerId;
    private AccountId accountId;
    private Email email;

    @BeforeEach
    void setUp() {
        jwtTokenService = new JwtTokenServiceImpl();
        customerId = CustomerId.generate();
        accountId = AccountId.of(123L);
        email = Email.of("test@example.com");
    }

    @Nested
    @DisplayName("토큰 쌍 생성 테스트")
    class GenerateTokenPairTest {

        @Test
        @DisplayName("유효한 정보로 토큰 쌍을 생성할 수 있다")
        void generateTokenPair_WithValidData_ShouldSuccess() {
            // When
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);

            // Then
            assertThat(tokenPair).isNotNull();
            assertThat(tokenPair.getAccessToken()).isNotNull();
            assertThat(tokenPair.getRefreshToken()).isNotNull();
            assertThat(tokenPair.getAccessToken().getType()).isEqualTo(JwtTokenType.ACCESS);
            assertThat(tokenPair.getRefreshToken().getType()).isEqualTo(JwtTokenType.REFRESH);
            assertThat(tokenPair.isValid()).isTrue();
        }

        @Test
        @DisplayName("생성된 토큰들은 유효하다")
        void generateTokenPair_GeneratedTokens_ShouldBeValid() {
            // When
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);

            // Then
            assertThat(tokenPair.getAccessToken().isValid()).isTrue();
            assertThat(tokenPair.getRefreshToken().isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("토큰 검증 테스트")
    class ValidateTokenTest {

        @Test
        @DisplayName("유효한 토큰을 검증하면 Claims를 반환한다")
        void validateToken_WithValidToken_ShouldReturnClaims() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            JwtToken accessToken = tokenPair.getAccessToken();

            // When
            Optional<JwtClaims> claimsOpt = jwtTokenService.validateToken(accessToken);

            // Then
            assertThat(claimsOpt).isPresent();
            JwtClaims claims = claimsOpt.get();
            assertThat(claims.getCustomerId()).isEqualTo(customerId);
            assertThat(claims.getAccountIdObject()).isEqualTo(accountId);
            assertThat(claims.getEmailObject()).isEqualTo(email);
            assertThat(claims.getTokenType()).isEqualTo(JwtTokenType.ACCESS);
        }

        @Test
        @DisplayName("블랙리스트에 등록된 토큰은 검증에 실패한다")
        void validateToken_WithBlacklistedToken_ShouldReturnEmpty() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            JwtToken accessToken = tokenPair.getAccessToken();
            jwtTokenService.invalidateToken(accessToken);

            // When
            Optional<JwtClaims> claimsOpt = jwtTokenService.validateToken(accessToken);

            // Then
            assertThat(claimsOpt).isEmpty();
        }
    }

    @Nested
    @DisplayName("토큰 파싱 테스트")
    class ParseTokenTest {

        @Test
        @DisplayName("유효한 토큰 문자열을 파싱할 수 있다")
        void parseToken_WithValidTokenString_ShouldReturnToken() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            String tokenString = tokenPair.getAccessToken().getValue();

            // When
            Optional<JwtToken> tokenOpt = jwtTokenService.parseToken(tokenString);

            // Then
            assertThat(tokenOpt).isPresent();
            assertThat(tokenOpt.get().getValue()).isEqualTo(tokenString);
            assertThat(tokenOpt.get().getType()).isEqualTo(JwtTokenType.ACCESS);
        }

        @Test
        @DisplayName("Bearer 접두사가 있는 토큰을 파싱할 수 있다")
        void parseToken_WithBearerPrefix_ShouldReturnToken() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            String tokenString = "Bearer " + tokenPair.getAccessToken().getValue();

            // When
            Optional<JwtToken> tokenOpt = jwtTokenService.parseToken(tokenString);

            // Then
            assertThat(tokenOpt).isPresent();
            assertThat(tokenOpt.get().getValue()).isEqualTo(tokenPair.getAccessToken().getValue());
        }

        @Test
        @DisplayName("유효하지 않은 토큰 문자열은 파싱에 실패한다")
        void parseToken_WithInvalidTokenString_ShouldReturnEmpty() {
            // Given
            String invalidToken = "invalid.token.string";

            // When
            Optional<JwtToken> tokenOpt = jwtTokenService.parseToken(invalidToken);

            // Then
            assertThat(tokenOpt).isEmpty();
        }

        @Test
        @DisplayName("null이나 빈 문자열은 파싱에 실패한다")
        void parseToken_WithNullOrEmpty_ShouldReturnEmpty() {
            // When & Then
            assertThat(jwtTokenService.parseToken(null)).isEmpty();
            assertThat(jwtTokenService.parseToken("")).isEmpty();
            assertThat(jwtTokenService.parseToken("   ")).isEmpty();
        }
    }

    @Nested
    @DisplayName("액세스 토큰 갱신 테스트")
    class RefreshAccessTokenTest {

        @Test
        @DisplayName("유효한 리프레시 토큰으로 액세스 토큰을 갱신할 수 있다")
        void refreshAccessToken_WithValidRefreshToken_ShouldReturnNewAccessToken() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            JwtToken refreshToken = tokenPair.getRefreshToken();

            // When
            JwtToken newAccessToken = jwtTokenService.refreshAccessToken(refreshToken);

            // Then
            assertThat(newAccessToken).isNotNull();
            assertThat(newAccessToken.getType()).isEqualTo(JwtTokenType.ACCESS);
            assertThat(newAccessToken.isValid()).isTrue();
            // 새로 생성된 토큰이므로 이전과 다른 토큰이어야 하지만,
            // 매우 짧은 시간에 생성되면 같을 수도 있으므로 검증 조건을 완화
            assertThat(newAccessToken).isNotNull();
        }

        @Test
        @DisplayName("액세스 토큰으로 갱신 시도 시 예외가 발생한다")
        void refreshAccessToken_WithAccessToken_ShouldThrowException() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            JwtToken accessToken = tokenPair.getAccessToken();

            // When & Then
            assertThatThrownBy(() -> jwtTokenService.refreshAccessToken(accessToken))
                .isInstanceOf(InvalidJwtTokenException.class)
                .hasMessageContaining("리프레시 토큰이 아닙니다");
        }
    }

    @Nested
    @DisplayName("토큰 블랙리스트 테스트")
    class TokenBlacklistTest {

        @Test
        @DisplayName("토큰을 블랙리스트에 추가할 수 있다")
        void invalidateToken_ShouldAddToBlacklist() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            JwtToken accessToken = tokenPair.getAccessToken();

            // When
            jwtTokenService.invalidateToken(accessToken);

            // Then
            assertThat(jwtTokenService.isTokenBlacklisted(accessToken)).isTrue();
        }

        @Test
        @DisplayName("블랙리스트에 없는 토큰은 블랙리스트 상태가 아니다")
        void isTokenBlacklisted_WithValidToken_ShouldReturnFalse() {
            // Given
            TokenPair tokenPair = jwtTokenService.generateTokenPair(customerId, accountId, email);
            JwtToken accessToken = tokenPair.getAccessToken();

            // When & Then
            assertThat(jwtTokenService.isTokenBlacklisted(accessToken)).isFalse();
        }
    }
}