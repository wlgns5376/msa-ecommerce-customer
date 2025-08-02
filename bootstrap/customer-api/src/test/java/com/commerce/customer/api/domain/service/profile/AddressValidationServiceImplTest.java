package com.commerce.customer.api.domain.service.profile;

import com.commerce.customer.core.domain.service.profile.AddressValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AddressValidationServiceImpl 단위 테스트")
class AddressValidationServiceImplTest {

    private AddressValidationService addressValidationService;

    @BeforeEach
    void setUp() {
        addressValidationService = new AddressValidationServiceImpl();
    }

    @Nested
    @DisplayName("주소 유효성 검증 테스트")
    class ValidateAddressTest {

        @Test
        @DisplayName("성공: 유효한 우편번호와 도로명 주소")
        void validateAddress_ValidZipCodeAndRoadAddress_ShouldReturnTrue() {
            // given
            String zipCode = "12345";
            String roadAddress = "서울특별시 강남구 테헤란로 123";

            // when
            boolean result = addressValidationService.validateAddress(zipCode, roadAddress);

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @DisplayName("성공: 다양한 유효한 우편번호 형식")
        @ValueSource(strings = {"12345", "54321", "98765", "11111", "00000"})
        void validateAddress_ValidZipCodes_ShouldReturnTrue(String zipCode) {
            // given
            String roadAddress = "서울특별시 강남구 테헤란로 123";

            // when
            boolean result = addressValidationService.validateAddress(zipCode, roadAddress);

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @DisplayName("성공: 유효한 도로명 주소 (최소 5자 이상)")
        @ValueSource(strings = {
            "서울특별시 강남구 테헤란로 123",
            "부산광역시 해운대구 센텀로 99",
            "대구광역시 중구 동성로 123",
            "최소길이주소",
            "   최소길이주소   " // 공백 포함 (trim 처리됨)
        })
        void validateAddress_ValidRoadAddresses_ShouldReturnTrue(String roadAddress) {
            // given
            String zipCode = "12345";

            // when
            boolean result = addressValidationService.validateAddress(zipCode, roadAddress);

            // then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @DisplayName("실패: null 또는 빈 값 입력")
        @NullAndEmptySource
        void validateAddress_NullOrEmptyInputs_ShouldReturnFalse(String input) {
            // when & then
            assertThat(addressValidationService.validateAddress(input, "서울특별시 강남구 테헤란로 123"))
                .isFalse();
            assertThat(addressValidationService.validateAddress("12345", input))
                .isFalse();
            assertThat(addressValidationService.validateAddress(input, input))
                .isFalse();
        }

        @ParameterizedTest
        @DisplayName("실패: 잘못된 우편번호 형식")
        @ValueSource(strings = {
            "1234",     // 4자리
            "123456",   // 6자리
            "1234a",    // 문자 포함
            "abcde",    // 모두 문자
            "12 345",   // 공백 포함
            "12-345",   // 하이픈 포함
            "",         // 빈 문자열
            "   "       // 공백만
        })
        void validateAddress_InvalidZipCodeFormat_ShouldReturnFalse(String invalidZipCode) {
            // given
            String roadAddress = "서울특별시 강남구 테헤란로 123";

            // when
            boolean result = addressValidationService.validateAddress(invalidZipCode, roadAddress);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @DisplayName("실패: 너무 짧은 도로명 주소 (5자 미만)")
        @ValueSource(strings = {
            "",        // 빈 문자열
            "가",      // 1자
            "가나",    // 2자
            "가나다",  // 3자
            "가나다라", // 4자
            "   ",     // 공백만 (trim 후 빈 문자열)
            "  가  "   // 공백 포함하여 trim 후 1자
        })
        void validateAddress_TooShortRoadAddress_ShouldReturnFalse(String shortAddress) {
            // given
            String zipCode = "12345";

            // when
            boolean result = addressValidationService.validateAddress(zipCode, shortAddress);

            // then
            assertThat(result).isFalse();
        }

        @ParameterizedTest
        @DisplayName("실패: 다양한 조합의 잘못된 입력")
        @CsvSource({
            "1234, 서울특별시 강남구 테헤란로 123",    // 잘못된 우편번호
            "12345, 짧음",                       // 너무 짧은 주소
            "abcde, 서울특별시 강남구 테헤란로 123",   // 문자 우편번호
            "12 345, 서울특별시 강남구 테헤란로 123"   // 공백 포함 우편번호
        })
        void validateAddress_InvalidCombinations_ShouldReturnFalse(String zipCode, String roadAddress) {
            // when
            boolean result = addressValidationService.validateAddress(zipCode, roadAddress);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("주소 제안 테스트")
    class SuggestAddressesTest {

        @Test
        @DisplayName("성공: 유효한 부분 주소로 제안 목록 반환")
        void suggestAddresses_ValidPartialAddress_ShouldReturnSuggestions() {
            // given
            String partialAddress = "서울시 강남구";

            // when
            List<String> suggestions = addressValidationService.suggestAddresses(partialAddress);

            // then
            assertThat(suggestions).isNotEmpty();
            assertThat(suggestions).hasSize(3);
            assertThat(suggestions).containsExactly(
                "서울시 강남구 1번길 10",
                "서울시 강남구 2번길 20",
                "서울시 강남구 3번길 30"
            );
        }

        @Test
        @DisplayName("성공: 짧은 부분 주소로도 제안 목록 반환")
        void suggestAddresses_ShortPartialAddress_ShouldReturnSuggestions() {
            // given
            String partialAddress = "서울";

            // when
            List<String> suggestions = addressValidationService.suggestAddresses(partialAddress);

            // then
            assertThat(suggestions).isNotEmpty();
            assertThat(suggestions).hasSize(3);
            assertThat(suggestions).allMatch(suggestion -> 
                suggestion.startsWith(partialAddress));
        }

        @Test
        @DisplayName("성공: 공백이 포함된 부분 주소 처리")
        void suggestAddresses_PartialAddressWithSpaces_ShouldReturnSuggestions() {
            // given
            String partialAddress = "  부산시 해운대구  ";

            // when
            List<String> suggestions = addressValidationService.suggestAddresses(partialAddress);

            // then
            assertThat(suggestions).isNotEmpty();
            assertThat(suggestions).hasSize(3);
        }

        @ParameterizedTest
        @DisplayName("실패: null 또는 빈 부분 주소")
        @NullAndEmptySource
        void suggestAddresses_NullOrEmptyPartialAddress_ShouldReturnEmptyList(String partialAddress) {
            // when
            List<String> suggestions = addressValidationService.suggestAddresses(partialAddress);

            // then
            assertThat(suggestions).isEmpty();
        }

        @Test
        @DisplayName("실패: 공백만 포함된 부분 주소")
        void suggestAddresses_OnlyWhitespacePartialAddress_ShouldReturnEmptyList() {
            // given
            String partialAddress = "   ";

            // when
            List<String> suggestions = addressValidationService.suggestAddresses(partialAddress);

            // then
            assertThat(suggestions).isEmpty();
        }
    }

    @Nested
    @DisplayName("주소 상세 정보 조회 테스트")
    class GetAddressDetailsTest {

        @Test
        @DisplayName("성공: 유효한 우편번호로 주소 상세 정보 반환")
        void getAddressDetails_ValidZipCode_ShouldReturnDetails() {
            // given
            String zipCode = "12345";

            // when
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails(zipCode);

            // then
            assertThat(details).isNotNull();
            assertThat(details.zipCode()).isEqualTo("12345");
            assertThat(details.city()).isEqualTo("서울특별시");
            assertThat(details.district()).isEqualTo("강남구");
            assertThat(details.roadName()).isEqualTo("테헤란로");
            assertThat(details.buildingNumbers()).isEqualTo("100-200");
        }

        @ParameterizedTest
        @DisplayName("성공: 다양한 유효한 우편번호")
        @ValueSource(strings = {"54321", "98765", "11111", "00000"})
        void getAddressDetails_VariousValidZipCodes_ShouldReturnDetails(String zipCode) {
            // when
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails(zipCode);

            // then
            assertThat(details).isNotNull();
            assertThat(details.zipCode()).isEqualTo(zipCode);
            assertThat(details.city()).isNotNull();
            assertThat(details.district()).isNotNull();
            assertThat(details.roadName()).isNotNull();
            assertThat(details.buildingNumbers()).isNotNull();
        }

        @ParameterizedTest
        @DisplayName("실패: null 또는 잘못된 우편번호 형식")
        @ValueSource(strings = {
            "1234",     // 4자리
            "123456",   // 6자리
            "1234a",    // 문자 포함
            "abcde",    // 모두 문자
            "12 345",   // 공백 포함
            "12-345"    // 하이픈 포함
        })
        void getAddressDetails_InvalidZipCode_ShouldReturnNull(String invalidZipCode) {
            // when
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails(invalidZipCode);

            // then
            assertThat(details).isNull();
        }

        @Test
        @DisplayName("실패: null 우편번호")
        void getAddressDetails_NullZipCode_ShouldReturnNull() {
            // when
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails(null);

            // then
            assertThat(details).isNull();
        }

        @Test
        @DisplayName("실패: 빈 문자열 우편번호")
        void getAddressDetails_EmptyZipCode_ShouldReturnNull() {
            // when
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails("");

            // then
            assertThat(details).isNull();
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTest {

        @Test
        @DisplayName("성공: 전체 주소 처리 시나리오")
        void fullAddressProcessingScenario_ShouldWorkCorrectly() {
            // given
            String zipCode = "12345";
            String roadAddress = "서울특별시 강남구 테헤란로 123";
            String partialAddress = "서울시 강남구";

            // when & then
            // 1. 주소 유효성 검증
            boolean isValid = addressValidationService.validateAddress(zipCode, roadAddress);
            assertThat(isValid).isTrue();

            // 2. 주소 제안
            List<String> suggestions = addressValidationService.suggestAddresses(partialAddress);
            assertThat(suggestions).isNotEmpty();

            // 3. 주소 상세 정보 조회
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails(zipCode);
            assertThat(details).isNotNull();
            assertThat(details.zipCode()).isEqualTo(zipCode);
        }

        @Test
        @DisplayName("실패: 잘못된 주소 처리 시나리오")
        void invalidAddressProcessingScenario_ShouldHandleGracefully() {
            // given
            String invalidZipCode = "1234a";
            String shortRoadAddress = "짧음";
            String emptyPartialAddress = "";

            // when & then
            // 1. 주소 유효성 검증 실패
            boolean isValid = addressValidationService.validateAddress(invalidZipCode, shortRoadAddress);
            assertThat(isValid).isFalse();

            // 2. 빈 주소 제안
            List<String> suggestions = addressValidationService.suggestAddresses(emptyPartialAddress);
            assertThat(suggestions).isEmpty();

            // 3. null 주소 상세 정보
            AddressValidationService.AddressDetails details = 
                addressValidationService.getAddressDetails(invalidZipCode);
            assertThat(details).isNull();
        }
    }
}