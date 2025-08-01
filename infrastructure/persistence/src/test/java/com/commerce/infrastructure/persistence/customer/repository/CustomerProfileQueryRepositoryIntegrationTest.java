package com.commerce.infrastructure.persistence.customer.repository;

import com.commerce.infrastructure.persistence.TestApplication;
import com.commerce.infrastructure.persistence.config.TestJpaConfig;
import com.commerce.infrastructure.persistence.customer.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes = TestApplication.class)
@Import({TestJpaConfig.class, CustomerProfileQueryRepository.class})
@DisplayName("CustomerProfileQueryRepository 통합 테스트")
@Transactional
class CustomerProfileQueryRepositoryIntegrationTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CustomerProfileQueryRepository repository;

    @Autowired
    private CustomerProfileJpaRepository jpaRepository;

    @Autowired
    private AddressJpaRepository addressRepository;

    @Autowired
    private BrandPreferenceJpaRepository brandPreferenceRepository;

    @Autowired
    private CategoryInterestJpaRepository categoryInterestRepository;

    private CustomerProfileEntity testProfile1;
    private CustomerProfileEntity testProfile2;
    private CustomerProfileEntity testProfile3;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 생성
        testProfile1 = createAndSaveProfile(
            1L, "홍", "길동", 
            LocalDate.of(1990, 1, 15), 
            CustomerProfileEntity.Gender.MALE,
            CustomerProfileEntity.ProfileStatus.ACTIVE
        );

        testProfile2 = createAndSaveProfile(
            2L, "김", "영희", 
            LocalDate.of(1985, 5, 20), 
            CustomerProfileEntity.Gender.FEMALE,
            CustomerProfileEntity.ProfileStatus.ACTIVE
        );

        testProfile3 = createAndSaveProfile(
            3L, "이", "철수", 
            LocalDate.of(1992, 12, 10), 
            CustomerProfileEntity.Gender.MALE,
            CustomerProfileEntity.ProfileStatus.INACTIVE
        );

        // 주소 추가
        addAddress(testProfile1, AddressEntity.AddressType.HOME, "집", "06234", 
                  "서울시 강남구 테헤란로 123", "서울시 강남구 대치동 123", "101동 1001호", true);
        addAddress(testProfile1, AddressEntity.AddressType.WORK, "회사", "06621", 
                  "서울시 서초구 서초대로 456", "서울시 서초구 서초동 456", "5층", false);
        addAddress(testProfile2, AddressEntity.AddressType.HOME, "집", "13494", 
                  "경기도 성남시 분당로 789", "경기도 성남시 분당구 789", "201호", true);

        // 브랜드 선호도 추가
        addBrandPreference(testProfile1, "Nike", BrandPreferenceEntity.PreferenceLevel.LOVE);
        addBrandPreference(testProfile1, "Adidas", BrandPreferenceEntity.PreferenceLevel.LIKE);
        addBrandPreference(testProfile2, "Nike", BrandPreferenceEntity.PreferenceLevel.LIKE);
        addBrandPreference(testProfile3, "Puma", BrandPreferenceEntity.PreferenceLevel.LOVE);

        // 카테고리 관심사 추가
        addCategoryInterest(testProfile1, "스포츠", CategoryInterestEntity.InterestLevel.HIGH);
        addCategoryInterest(testProfile1, "패션", CategoryInterestEntity.InterestLevel.MEDIUM);
        addCategoryInterest(testProfile2, "뷰티", CategoryInterestEntity.InterestLevel.HIGH);
        addCategoryInterest(testProfile2, "스포츠", CategoryInterestEntity.InterestLevel.LOW);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("ID로 모든 상세 정보와 함께 프로필을 조회한다")
    void findWithAllDetailsById_Success() {
        // When
        Optional<CustomerProfileEntity> result = repository.findWithAllDetailsById(testProfile1.getCustomerId());

        // Then
        assertThat(result).isPresent();
        CustomerProfileEntity profile = result.get();
        assertThat(profile.getCustomerId()).isEqualTo(testProfile1.getCustomerId());
        assertThat(profile.getAddresses()).hasSize(2);
        assertThat(profile.getBrandPreferences()).hasSize(2);
        assertThat(profile.getCategoryInterests()).hasSize(2);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 빈 Optional을 반환한다")
    void findWithAllDetailsById_NotFound() {
        // When
        Optional<CustomerProfileEntity> result = repository.findWithAllDetailsById(999L);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("모든 검색 조건을 사용하여 프로필을 검색한다")
    void findBySearchConditions_AllConditions() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CustomerProfileEntity> result = repository.findBySearchConditions(
            "홍", "길동",
            LocalDate.of(1989, 1, 1), LocalDate.of(1991, 12, 31),
            CustomerProfileEntity.Gender.MALE,
            CustomerProfileEntity.ProfileStatus.ACTIVE,
            pageable
        );

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCustomerId()).isEqualTo(testProfile1.getCustomerId());
    }

    @ParameterizedTest
    @DisplayName("부분 검색 조건으로 프로필을 검색한다")
    @MethodSource("provideSearchConditions")
    void findBySearchConditions_PartialConditions(
            String firstName, String lastName,
            LocalDate birthDateFrom, LocalDate birthDateTo,
            CustomerProfileEntity.Gender gender,
            CustomerProfileEntity.ProfileStatus status,
            int expectedCount) {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<CustomerProfileEntity> result = repository.findBySearchConditions(
            firstName, lastName, birthDateFrom, birthDateTo, gender, status, pageable
        );

        // Then
        assertThat(result.getTotalElements()).isEqualTo(expectedCount);
    }

    private static Stream<Arguments> provideSearchConditions() {
        return Stream.of(
            // firstName만 검색
            Arguments.of("홍", null, null, null, null, null, 1),
            // lastName만 검색
            Arguments.of(null, "영희", null, null, null, null, 1),
            // 성별만 검색
            Arguments.of(null, null, null, null, CustomerProfileEntity.Gender.MALE, null, 2),
            // 상태만 검색
            Arguments.of(null, null, null, null, null, CustomerProfileEntity.ProfileStatus.ACTIVE, 2),
            // 생년월일 범위만 검색
            Arguments.of(null, null, LocalDate.of(1980, 1, 1), LocalDate.of(1990, 12, 31), null, null, 2),
            // 빈 문자열은 무시되어야 함
            Arguments.of("", "", null, null, null, null, 3),
            // 공백 문자열도 무시되어야 함
            Arguments.of("  ", "  ", null, null, null, null, 3)
        );
    }

    @Test
    @DisplayName("특정 브랜드를 선호하는 고객을 선호도 순으로 조회한다")
    void findByPreferredBrand_Success() {
        // When
        List<CustomerProfileEntity> result = repository.findByPreferredBrand("Nike", 10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCustomerId()).isEqualTo(testProfile1.getCustomerId()); // 선호도 LOVE
        assertThat(result.get(1).getCustomerId()).isEqualTo(testProfile2.getCustomerId()); // 선호도 LIKE
    }

    @Test
    @DisplayName("존재하지 않는 브랜드로 조회 시 빈 리스트를 반환한다")
    void findByPreferredBrand_NotFound() {
        // When
        List<CustomerProfileEntity> result = repository.findByPreferredBrand("Unknown", 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("limit 파라미터가 결과를 제한한다")
    void findByPreferredBrand_WithLimit() {
        // When
        List<CustomerProfileEntity> result = repository.findByPreferredBrand("Nike", 1);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(testProfile1.getCustomerId());
    }

    @Test
    @DisplayName("특정 카테고리에 관심있는 고객을 관심도 순으로 조회한다")
    void findByInterestCategory_Success() {
        // When
        List<CustomerProfileEntity> result = repository.findByInterestCategory("스포츠", 10);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCustomerId()).isEqualTo(testProfile1.getCustomerId()); // 관심도 HIGH
        assertThat(result.get(1).getCustomerId()).isEqualTo(testProfile2.getCustomerId()); // 관심도 LOW
    }

    @Test
    @DisplayName("최근 활성 고객을 업데이트 시간 역순으로 조회한다")
    void findRecentlyActiveCustomers_Success() {
        // Given
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);

        // When
        List<CustomerProfileEntity> result = repository.findRecentlyActiveCustomers(oneWeekAgo, 10);

        // Then
        assertThat(result).hasSize(2); // ACTIVE 상태인 고객만
        assertThat(result).extracting("status")
            .containsOnly(CustomerProfileEntity.ProfileStatus.ACTIVE);
    }

    @Test
    @DisplayName("미래 시점으로 조회 시 빈 리스트를 반환한다")
    void findRecentlyActiveCustomers_FutureDate() {
        // Given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);

        // When
        List<CustomerProfileEntity> result = repository.findRecentlyActiveCustomers(tomorrow, 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("생일이 특정 기간 내인 활성 고객을 조회한다")
    void findCustomersWithBirthdayInRange_Success() {
        // Given
        LocalDate startDate = LocalDate.of(1990, 1, 1);
        LocalDate endDate = LocalDate.of(1990, 12, 31);

        // When
        List<CustomerProfileEntity> result = repository.findCustomersWithBirthdayInRange(startDate, endDate);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCustomerId()).isEqualTo(testProfile1.getCustomerId());
        assertThat(result.get(0).getBirthDate()).isBetween(startDate, endDate);
    }

    @Test
    @DisplayName("생일 범위에 해당하는 고객이 없으면 빈 리스트를 반환한다")
    void findCustomersWithBirthdayInRange_NoMatch() {
        // Given
        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2000, 12, 31);

        // When
        List<CustomerProfileEntity> result = repository.findCustomersWithBirthdayInRange(startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성별별 고객 수 통계를 조회한다")
    void getCustomerCountByGender_Success() {
        // When
        List<Object[]> result = repository.getCustomerCountByGender();

        // Then
        assertThat(result).hasSize(2); // MALE, FEMALE
        
        for (Object[] row : result) {
            CustomerProfileEntity.Gender gender = (CustomerProfileEntity.Gender) row[0];
            Long count = (Long) row[1];
            
            if (gender == CustomerProfileEntity.Gender.MALE) {
                assertThat(count).isEqualTo(1L); // 활성 상태인 남성은 1명
            } else if (gender == CustomerProfileEntity.Gender.FEMALE) {
                assertThat(count).isEqualTo(1L); // 활성 상태인 여성은 1명
            }
        }
    }

    @Test
    @DisplayName("페이징이 올바르게 동작한다")
    void findBySearchConditions_Paging() {
        // Given
        Pageable firstPage = PageRequest.of(0, 1);
        Pageable secondPage = PageRequest.of(1, 1);

        // When
        Page<CustomerProfileEntity> firstResult = repository.findBySearchConditions(
            null, null, null, null, null, 
            CustomerProfileEntity.ProfileStatus.ACTIVE, firstPage
        );
        Page<CustomerProfileEntity> secondResult = repository.findBySearchConditions(
            null, null, null, null, null, 
            CustomerProfileEntity.ProfileStatus.ACTIVE, secondPage
        );

        // Then
        assertThat(firstResult.getTotalElements()).isEqualTo(2);
        assertThat(firstResult.getTotalPages()).isEqualTo(2);
        assertThat(firstResult.getContent()).hasSize(1);
        
        assertThat(secondResult.getContent()).hasSize(1);
        assertThat(secondResult.getContent().get(0).getCustomerId())
            .isNotEqualTo(firstResult.getContent().get(0).getCustomerId());
    }

    // Helper methods
    private CustomerProfileEntity createAndSaveProfile(
            Long customerId, String firstName, String lastName,
            LocalDate birthDate, CustomerProfileEntity.Gender gender,
            CustomerProfileEntity.ProfileStatus status) {
        
        CustomerProfileEntity profile = CustomerProfileEntity.builder()
            .customerId(customerId)
            .firstName(firstName)
            .lastName(lastName)
            .birthDate(birthDate)
            .gender(gender)
            .status(status)
            .primaryPhone("010-1234-567" + customerId)
            .emailMarketingConsent(false)
            .smsMarketingConsent(false)
            .pushMarketingConsent(false)
            .orderNotifications(true)
            .promotionNotifications(false)
            .accountNotifications(true)
            .reviewNotifications(false)
            .build();
        
        return jpaRepository.save(profile);
    }

    private void addAddress(CustomerProfileEntity profile, AddressEntity.AddressType type, String alias, 
                          String zipCode, String roadAddress, String jibunAddress, 
                          String detailAddress, boolean isDefault) {
        AddressEntity address = AddressEntity.builder()
            .customerProfile(profile)
            .type(type)
            .alias(alias)
            .zipCode(zipCode)
            .roadAddress(roadAddress)
            .jibunAddress(jibunAddress)
            .detailAddress(detailAddress)
            .isDefault(isDefault)
            .build();
        
        addressRepository.save(address);
        profile.addAddress(address);
    }

    private void addBrandPreference(CustomerProfileEntity profile, String brandName, BrandPreferenceEntity.PreferenceLevel level) {
        BrandPreferenceEntity preference = BrandPreferenceEntity.builder()
            .customerProfile(profile)
            .brandName(brandName)
            .preferenceLevel(level)
            .build();
        
        brandPreferenceRepository.save(preference);
        profile.addBrandPreference(preference);
    }

    private void addCategoryInterest(CustomerProfileEntity profile, String categoryName, CategoryInterestEntity.InterestLevel level) {
        CategoryInterestEntity interest = CategoryInterestEntity.builder()
            .customerProfile(profile)
            .categoryName(categoryName)
            .interestLevel(level)
            .build();
        
        categoryInterestRepository.save(interest);
        profile.addCategoryInterest(interest);
    }
}