package com.commerce.infrastructure.persistence.integration;

import com.commerce.customer.core.domain.model.account.AccountId;
import com.commerce.customer.core.domain.model.profile.*;
import com.commerce.customer.core.domain.repository.CustomerProfileRepository;
import com.commerce.infrastructure.persistence.customer.adapter.CustomerProfileRepositoryAdapter;
import com.commerce.infrastructure.persistence.customer.entity.CustomerProfileEntity;
import com.commerce.infrastructure.persistence.customer.mapper.CustomerProfileMapper;
import com.commerce.infrastructure.persistence.customer.repository.CustomerProfileJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.utility.DockerImageName.parse;

@DataJpaTest
@ActiveProfiles("integration")
@Import({CustomerProfileRepositoryAdapter.class, CustomerProfileMapper.class})
@DisplayName("CustomerProfileRepository 통합 테스트")
@Testcontainers
class CustomerProfileRepositoryIntegrationTest {

    @Container
    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>(parse("mariadb:11.1"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
        registry.add("spring.datasource.driver-class-name", mariaDB::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MariaDBDialect");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CustomerProfileJpaRepository customerProfileJpaRepository;

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @BeforeEach
    void setUp() {
        // TestContainers 시작 대기
        mariaDB.isRunning();
    }

    @Test
    @DisplayName("고객 프로필 저장 및 조회가 정상적으로 동작한다")
    @Transactional
    void saveAndFind_Success() {
        // Given
        PersonalInfo personalInfo = new PersonalInfo(
            "홍", "길동", 
            LocalDate.of(1990, 1, 1), 
            "MALE"
        );
        
        ContactInfo contactInfo = new ContactInfo("010-1234-5678");
        
        MarketingConsent marketingConsent = new MarketingConsent(
            true, true, true, true
        );
        
        NotificationSettings notificationSettings = new NotificationSettings(
            true, true, true, true
        );
        
        ProfilePreferences preferences = new ProfilePreferences(
            Set.of(new BrandPreference("Nike"), new BrandPreference("Adidas")),
            Set.of(new CategoryPreference("운동화"), new CategoryPreference("의류")),
            new PriceRangePreference(10000, 100000)
        );
        
        CustomerProfile profile = CustomerProfile.create(
            new ProfileId(1L),
            new AccountId(1L),
            personalInfo,
            contactInfo,
            marketingConsent,
            notificationSettings,
            preferences
        );

        // When
        CustomerProfile savedProfile = customerProfileRepository.save(profile);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<CustomerProfile> foundProfile = customerProfileRepository.findById(savedProfile.getProfileId());
        
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getPersonalInfo().getFirstName()).isEqualTo("홍");
        assertThat(foundProfile.get().getPersonalInfo().getLastName()).isEqualTo("길동");
        assertThat(foundProfile.get().getContactInfo().getPhone()).isEqualTo("010-1234-5678");
        assertThat(foundProfile.get().getAccountId().getValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("계정 ID로 고객 프로필 조회가 정상적으로 동작한다")
    @Transactional
    void findByAccountId_Success() {
        // Given
        CustomerProfileEntity profileEntity = CustomerProfileEntity.builder()
                .accountId(100L)
                .firstName("김")
                .lastName("테스트")
                .phone("010-9876-5432")
                .birthDate(LocalDate.of(1985, 5, 15))
                .gender("FEMALE")
                .emailMarketingConsent(true)
                .smsMarketingConsent(false)
                .pushMarketingConsent(true)
                .mailMarketingConsent(false)
                .emailNotificationEnabled(true)
                .smsNotificationEnabled(true)
                .pushNotificationEnabled(false)
                .inAppNotificationEnabled(true)
                .build();
        
        entityManager.persistAndFlush(profileEntity);
        entityManager.clear();

        // When
        Optional<CustomerProfile> foundProfile = customerProfileRepository.findByAccountId(new AccountId(100L));

        // Then
        assertThat(foundProfile).isPresent();
        assertThat(foundProfile.get().getPersonalInfo().getFirstName()).isEqualTo("김");
        assertThat(foundProfile.get().getPersonalInfo().getLastName()).isEqualTo("테스트");
        assertThat(foundProfile.get().getContactInfo().getPhone()).isEqualTo("010-9876-5432");
        assertThat(foundProfile.get().getAccountId().getValue()).isEqualTo(100L);
    }

    @Test
    @DisplayName("고객 프로필 존재 여부 확인이 정상적으로 동작한다")
    @Transactional
    void existsByAccountId_Success() {
        // Given
        CustomerProfileEntity profileEntity = CustomerProfileEntity.builder()
                .accountId(200L)
                .firstName("이")
                .lastName("존재")
                .phone("010-1111-2222")
                .birthDate(LocalDate.of(1992, 3, 10))
                .gender("MALE")
                .emailMarketingConsent(true)
                .smsMarketingConsent(true)
                .pushMarketingConsent(true)
                .mailMarketingConsent(true)
                .emailNotificationEnabled(true)
                .smsNotificationEnabled(true)
                .pushNotificationEnabled(true)
                .inAppNotificationEnabled(true)
                .build();
        
        entityManager.persistAndFlush(profileEntity);

        // When & Then
        assertThat(customerProfileRepository.existsByAccountId(new AccountId(200L))).isTrue();
        assertThat(customerProfileRepository.existsByAccountId(new AccountId(999L))).isFalse();
    }

    @Test
    @DisplayName("고객 프로필 삭제가 정상적으로 동작한다")
    @Transactional
    void deleteProfile_Success() {
        // Given
        CustomerProfileEntity profileEntity = CustomerProfileEntity.builder()
                .accountId(300L)
                .firstName("박")
                .lastName("삭제")
                .phone("010-3333-4444")
                .birthDate(LocalDate.of(1988, 8, 20))
                .gender("FEMALE")
                .emailMarketingConsent(false)
                .smsMarketingConsent(false)
                .pushMarketingConsent(false)
                .mailMarketingConsent(false)
                .emailNotificationEnabled(false)
                .smsNotificationEnabled(false)
                .pushNotificationEnabled(false)
                .inAppNotificationEnabled(false)
                .build();
        
        entityManager.persistAndFlush(profileEntity);
        Long profileId = profileEntity.getProfileId();

        // When
        customerProfileRepository.deleteById(new ProfileId(profileId));
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<CustomerProfile> foundProfile = customerProfileRepository.findById(new ProfileId(profileId));
        assertThat(foundProfile).isEmpty();
    }

    @Test
    @DisplayName("고객 프로필 업데이트가 정상적으로 동작한다")
    @Transactional
    void updateProfile_Success() {
        // Given
        CustomerProfileEntity profileEntity = CustomerProfileEntity.builder()
                .accountId(400L)
                .firstName("최")
                .lastName("업데이트")
                .phone("010-5555-6666")
                .birthDate(LocalDate.of(1995, 12, 5))
                .gender("MALE")
                .emailMarketingConsent(true)
                .smsMarketingConsent(false)
                .pushMarketingConsent(true)
                .mailMarketingConsent(false)
                .emailNotificationEnabled(true)
                .smsNotificationEnabled(false)
                .pushNotificationEnabled(true)
                .inAppNotificationEnabled(false)
                .build();
        
        entityManager.persistAndFlush(profileEntity);
        entityManager.clear();

        // When - 프로필 조회 후 업데이트
        Optional<CustomerProfile> foundProfile = customerProfileRepository.findByAccountId(new AccountId(400L));
        assertThat(foundProfile).isPresent();
        
        CustomerProfile profile = foundProfile.get();
        
        // 새로운 개인정보로 업데이트
        PersonalInfo newPersonalInfo = new PersonalInfo(
            "최", "수정됨", 
            LocalDate.of(1995, 12, 5), 
            "MALE"
        );
        
        ContactInfo newContactInfo = new ContactInfo("010-7777-8888");
        
        CustomerProfile updatedProfile = CustomerProfile.create(
            profile.getProfileId(),
            profile.getAccountId(),
            newPersonalInfo,
            newContactInfo,
            profile.getMarketingConsent(),
            profile.getNotificationSettings(),
            profile.getPreferences()
        );
        
        customerProfileRepository.save(updatedProfile);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<CustomerProfile> reloadedProfile = customerProfileRepository.findByAccountId(new AccountId(400L));
        assertThat(reloadedProfile).isPresent();
        assertThat(reloadedProfile.get().getPersonalInfo().getLastName()).isEqualTo("수정됨");
        assertThat(reloadedProfile.get().getContactInfo().getPhone()).isEqualTo("010-7777-8888");
    }

    @Test
    @DisplayName("마케팅 동의 및 알림 설정이 정상적으로 저장된다")
    @Transactional
    void marketingConsentAndNotificationSettings_Success() {
        // Given
        PersonalInfo personalInfo = new PersonalInfo(
            "정", "설정", 
            LocalDate.of(1993, 7, 25), 
            "FEMALE"
        );
        
        ContactInfo contactInfo = new ContactInfo("010-8888-9999");
        
        MarketingConsent marketingConsent = new MarketingConsent(
            true, false, true, false
        );
        
        NotificationSettings notificationSettings = new NotificationSettings(
            false, true, false, true
        );
        
        ProfilePreferences preferences = new ProfilePreferences(
            Set.of(new BrandPreference("Samsung")),
            Set.of(new CategoryPreference("전자제품")),
            new PriceRangePreference(50000, 500000)
        );
        
        CustomerProfile profile = CustomerProfile.create(
            new ProfileId(1L),
            new AccountId(500L),
            personalInfo,
            contactInfo,
            marketingConsent,
            notificationSettings,
            preferences
        );

        // When
        CustomerProfile savedProfile = customerProfileRepository.save(profile);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<CustomerProfile> foundProfile = customerProfileRepository.findById(savedProfile.getProfileId());
        
        assertThat(foundProfile).isPresent();
        CustomerProfile result = foundProfile.get();
        
        // 마케팅 동의 확인
        assertThat(result.getMarketingConsent().isEmailMarketingConsent()).isTrue();
        assertThat(result.getMarketingConsent().isSmsMarketingConsent()).isFalse();
        assertThat(result.getMarketingConsent().isPushMarketingConsent()).isTrue();
        assertThat(result.getMarketingConsent().isMailMarketingConsent()).isFalse();
        
        // 알림 설정 확인
        assertThat(result.getNotificationSettings().isEmailNotificationEnabled()).isFalse();
        assertThat(result.getNotificationSettings().isSmsNotificationEnabled()).isTrue();
        assertThat(result.getNotificationSettings().isPushNotificationEnabled()).isFalse();
        assertThat(result.getNotificationSettings().isInAppNotificationEnabled()).isTrue();
    }
}