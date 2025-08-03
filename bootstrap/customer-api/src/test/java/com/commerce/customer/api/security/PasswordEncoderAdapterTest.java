package com.commerce.customer.api.security;

import com.commerce.customer.api.security.adapter.PasswordEncoderAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordEncoderAdapter 단위 테스트")
class PasswordEncoderAdapterTest {

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private PasswordEncoderAdapter passwordEncoderAdapter;

    private static final String RAW_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "$2a$10$N9qo8uLOickgx2ZMRZoMye1IcZwN42PYQhDsxYU2.P/kLMjBhRG/K";

    @Nested
    @DisplayName("encode 메서드 테스트")
    class EncodeTest {

        @Test
        @DisplayName("원시 패스워드를 인코딩하여 반환")
        void givenRawPassword_whenEncode_thenReturnEncodedPassword() {
            // Given
            given(bCryptPasswordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);

            // When
            String result = passwordEncoderAdapter.encode(RAW_PASSWORD);

            // Then
            assertThat(result).isEqualTo(ENCODED_PASSWORD);
            then(bCryptPasswordEncoder).should().encode(RAW_PASSWORD);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "password123",
            "!@#$%^&*()",
            "1234567890",
            "Password123!",
            "한글패스워드123",
            "very_long_password_with_special_characters_!@#$%^&*()_1234567890"
        })
        @DisplayName("다양한 패스워드 인코딩 테스트")
        void givenVariousPasswords_whenEncode_thenDelegateToEncoder(String password) {
            // Given
            String expectedEncodedPassword = "encoded_" + password;
            given(bCryptPasswordEncoder.encode(password)).willReturn(expectedEncodedPassword);

            // When
            String result = passwordEncoderAdapter.encode(password);

            // Then
            assertThat(result).isEqualTo(expectedEncodedPassword);
            then(bCryptPasswordEncoder).should().encode(password);
        }

        @Test
        @DisplayName("빈 문자열 패스워드 인코딩")
        void givenEmptyPassword_whenEncode_thenDelegateToEncoder() {
            // Given
            String emptyPassword = "";
            String expectedEncodedPassword = "encoded_empty";
            given(bCryptPasswordEncoder.encode(emptyPassword)).willReturn(expectedEncodedPassword);

            // When
            String result = passwordEncoderAdapter.encode(emptyPassword);

            // Then
            assertThat(result).isEqualTo(expectedEncodedPassword);
            then(bCryptPasswordEncoder).should().encode(emptyPassword);
        }

        @Test
        @DisplayName("null 패스워드 인코딩 시 예외 발생")
        void givenNullPassword_whenEncode_thenThrowException() {
            // Given
            given(bCryptPasswordEncoder.encode(null)).willThrow(new IllegalArgumentException("패스워드는 null일 수 없습니다"));

            // When & Then
            assertThatThrownBy(() -> passwordEncoderAdapter.encode(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("패스워드는 null일 수 없습니다");
            
            then(bCryptPasswordEncoder).should().encode(null);
        }

        @Test
        @DisplayName("인코더에서 예외 발생 시 전파")
        void givenEncoderThrowsException_whenEncode_thenExceptionPropagated() {
            // Given
            RuntimeException encodingException = new RuntimeException("인코딩 실패");
            given(bCryptPasswordEncoder.encode(RAW_PASSWORD)).willThrow(encodingException);

            // When & Then
            assertThatThrownBy(() -> passwordEncoderAdapter.encode(RAW_PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("인코딩 실패");
            
            then(bCryptPasswordEncoder).should().encode(RAW_PASSWORD);
        }
    }

    @Nested
    @DisplayName("matches 메서드 테스트")
    class MatchesTest {

        @Test
        @DisplayName("일치하는 패스워드에 대해 true 반환")
        void givenMatchingPasswords_whenMatches_thenReturnTrue() {
            // Given
            given(bCryptPasswordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);

            // When
            boolean result = passwordEncoderAdapter.matches(RAW_PASSWORD, ENCODED_PASSWORD);

            // Then
            assertThat(result).isTrue();
            then(bCryptPasswordEncoder).should().matches(RAW_PASSWORD, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("일치하지 않는 패스워드에 대해 false 반환")
        void givenNonMatchingPasswords_whenMatches_thenReturnFalse() {
            // Given
            String wrongPassword = "wrongPassword";
            given(bCryptPasswordEncoder.matches(wrongPassword, ENCODED_PASSWORD)).willReturn(false);

            // When
            boolean result = passwordEncoderAdapter.matches(wrongPassword, ENCODED_PASSWORD);

            // Then
            assertThat(result).isFalse();
            then(bCryptPasswordEncoder).should().matches(wrongPassword, ENCODED_PASSWORD);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "wrongPassword",
            "Password123",
            "password123!@",
            "PASSWORD123!",
            "passworD123!",
            ""
        })
        @DisplayName("다양한 잘못된 패스워드에 대해 false 반환")
        void givenVariousWrongPasswords_whenMatches_thenReturnFalse(String wrongPassword) {
            // Given
            given(bCryptPasswordEncoder.matches(wrongPassword, ENCODED_PASSWORD)).willReturn(false);

            // When
            boolean result = passwordEncoderAdapter.matches(wrongPassword, ENCODED_PASSWORD);

            // Then
            assertThat(result).isFalse();
            then(bCryptPasswordEncoder).should().matches(wrongPassword, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("null 원시 패스워드에 대해 처리")
        void givenNullRawPassword_whenMatches_thenDelegateToEncoder() {
            // Given
            given(bCryptPasswordEncoder.matches(null, ENCODED_PASSWORD)).willReturn(false);

            // When
            boolean result = passwordEncoderAdapter.matches(null, ENCODED_PASSWORD);

            // Then
            assertThat(result).isFalse();
            then(bCryptPasswordEncoder).should().matches(null, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("null 인코딩된 패스워드에 대해 처리")
        void givenNullEncodedPassword_whenMatches_thenDelegateToEncoder() {
            // Given
            given(bCryptPasswordEncoder.matches(RAW_PASSWORD, null)).willReturn(false);

            // When
            boolean result = passwordEncoderAdapter.matches(RAW_PASSWORD, null);

            // Then
            assertThat(result).isFalse();
            then(bCryptPasswordEncoder).should().matches(RAW_PASSWORD, null);
        }

        @Test
        @DisplayName("둘 다 null인 경우 처리")
        void givenBothPasswordsNull_whenMatches_thenDelegateToEncoder() {
            // Given
            given(bCryptPasswordEncoder.matches(null, null)).willReturn(false);

            // When
            boolean result = passwordEncoderAdapter.matches(null, null);

            // Then
            assertThat(result).isFalse();
            then(bCryptPasswordEncoder).should().matches(null, null);
        }

        @Test
        @DisplayName("빈 문자열 패스워드 매칭")
        void givenEmptyPasswords_whenMatches_thenDelegateToEncoder() {
            // Given
            String emptyPassword = "";
            String emptyEncodedPassword = "";
            given(bCryptPasswordEncoder.matches(emptyPassword, emptyEncodedPassword)).willReturn(true);

            // When
            boolean result = passwordEncoderAdapter.matches(emptyPassword, emptyEncodedPassword);

            // Then
            assertThat(result).isTrue();
            then(bCryptPasswordEncoder).should().matches(emptyPassword, emptyEncodedPassword);
        }

        @Test
        @DisplayName("잘못된 형식의 인코딩된 패스워드 처리")
        void givenInvalidEncodedPassword_whenMatches_thenDelegateToEncoder() {
            // Given
            String invalidEncodedPassword = "invalid_format";
            given(bCryptPasswordEncoder.matches(RAW_PASSWORD, invalidEncodedPassword)).willReturn(false);

            // When
            boolean result = passwordEncoderAdapter.matches(RAW_PASSWORD, invalidEncodedPassword);

            // Then
            assertThat(result).isFalse();
            then(bCryptPasswordEncoder).should().matches(RAW_PASSWORD, invalidEncodedPassword);
        }

        @Test
        @DisplayName("매칭 과정에서 예외 발생 시 전파")
        void givenMatchingThrowsException_whenMatches_thenExceptionPropagated() {
            // Given
            RuntimeException matchingException = new RuntimeException("매칭 실패");
            given(bCryptPasswordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willThrow(matchingException);

            // When & Then
            assertThatThrownBy(() -> passwordEncoderAdapter.matches(RAW_PASSWORD, ENCODED_PASSWORD))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("매칭 실패");
            
            then(bCryptPasswordEncoder).should().matches(RAW_PASSWORD, ENCODED_PASSWORD);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("패스워드 인코딩 후 매칭 검증")
        void givenPassword_whenEncodeAndMatch_thenSuccessfulFlow() {
            // Given
            given(bCryptPasswordEncoder.encode(RAW_PASSWORD)).willReturn(ENCODED_PASSWORD);
            given(bCryptPasswordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);

            // When
            String encodedResult = passwordEncoderAdapter.encode(RAW_PASSWORD);
            boolean matchResult = passwordEncoderAdapter.matches(RAW_PASSWORD, encodedResult);

            // Then
            assertThat(encodedResult).isEqualTo(ENCODED_PASSWORD);
            assertThat(matchResult).isTrue();
            then(bCryptPasswordEncoder).should().encode(RAW_PASSWORD);
            then(bCryptPasswordEncoder).should().matches(RAW_PASSWORD, ENCODED_PASSWORD);
        }

        @Test
        @DisplayName("서로 다른 패스워드 인코딩 후 매칭 실패")
        void givenDifferentPasswords_whenEncodeAndMatch_thenMatchingFails() {
            // Given
            String password1 = "password1";
            String password2 = "password2";
            String encodedPassword1 = "encoded_password1";
            
            given(bCryptPasswordEncoder.encode(password1)).willReturn(encodedPassword1);
            given(bCryptPasswordEncoder.matches(password2, encodedPassword1)).willReturn(false);

            // When
            String encodedResult = passwordEncoderAdapter.encode(password1);
            boolean matchResult = passwordEncoderAdapter.matches(password2, encodedResult);

            // Then
            assertThat(encodedResult).isEqualTo(encodedPassword1);
            assertThat(matchResult).isFalse();
            then(bCryptPasswordEncoder).should().encode(password1);
            then(bCryptPasswordEncoder).should().matches(password2, encodedPassword1);
        }
    }
}