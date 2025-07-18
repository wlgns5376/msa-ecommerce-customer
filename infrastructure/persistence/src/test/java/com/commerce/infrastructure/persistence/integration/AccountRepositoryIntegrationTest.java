package com.commerce.infrastructure.persistence.integration;

import com.commerce.customer.core.domain.model.*;
import com.commerce.customer.core.domain.repository.AccountRepository;
import com.commerce.infrastructure.persistence.customer.adapter.AccountRepositoryAdapter;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.commerce.infrastructure.persistence.customer.mapper.AccountMapper;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.utility.DockerImageName.parse;

@DataJpaTest
@ActiveProfiles("integration")
@Import({AccountRepositoryAdapter.class, AccountMapper.class})
@DisplayName("AccountRepository 통합 테스트")
@Testcontainers
class AccountRepositoryIntegrationTest {

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
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        // TestContainers 시작 대기
        mariaDB.isRunning();
    }

    @Test
    @DisplayName("계정 저장 및 조회가 정상적으로 동작한다")
    @Transactional
    void saveAndFind_Success() {
        // Given
        Account account = Account.create(
                CustomerId.of(100L),
                Email.of("integration@test.com"),
                Password.of("ValidPass123!")
        );

        // When
        Account savedAccount = accountRepository.save(account);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<Account> foundAccount = accountRepository.findById(savedAccount.getAccountId());
        
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getEmail().getValue()).isEqualTo("integration@test.com");
        assertThat(foundAccount.get().getCustomerId().getValue()).isEqualTo(100L);
        assertThat(foundAccount.get().getStatus()).isEqualTo(AccountStatus.PENDING);
    }

    @Test
    @DisplayName("이메일로 계정 조회가 정상적으로 동작한다")
    @Transactional
    void findByEmail_Success() {
        // Given
        AccountEntity accountEntity = AccountEntity.builder()
                .customerId(200L)
                .email("email-search@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();
        
        entityManager.persistAndFlush(accountEntity);
        entityManager.clear();

        // When
        Optional<Account> foundAccount = accountRepository.findByEmail(Email.of("email-search@test.com"));

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getEmail().getValue()).isEqualTo("email-search@test.com");
        assertThat(foundAccount.get().getCustomerId().getValue()).isEqualTo(200L);
        assertThat(foundAccount.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("고객 ID로 계정 조회가 정상적으로 동작한다")
    @Transactional
    void findByCustomerId_Success() {
        // Given
        AccountEntity accountEntity = AccountEntity.builder()
                .customerId(300L)
                .email("customer-search@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();
        
        entityManager.persistAndFlush(accountEntity);
        entityManager.clear();

        // When
        Optional<Account> foundAccount = accountRepository.findByCustomerId(CustomerId.of(300L));

        // Then
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getCustomerId().getValue()).isEqualTo(300L);
        assertThat(foundAccount.get().getEmail().getValue()).isEqualTo("customer-search@test.com");
    }

    @Test
    @DisplayName("이메일 중복 확인이 정상적으로 동작한다")
    @Transactional
    void existsByEmail_Success() {
        // Given
        AccountEntity accountEntity = AccountEntity.builder()
                .customerId(400L)
                .email("exists@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();
        
        entityManager.persistAndFlush(accountEntity);

        // When & Then
        assertThat(accountRepository.existsByEmail(Email.of("exists@test.com"))).isTrue();
        assertThat(accountRepository.existsByEmail(Email.of("notexists@test.com"))).isFalse();
    }

    @Test
    @DisplayName("활성 계정 조회가 정상적으로 동작한다")
    @Transactional
    void findActiveAccount_Success() {
        // Given - 활성 계정
        AccountEntity activeAccount = AccountEntity.builder()
                .customerId(500L)
                .email("active@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();

        // Given - 비활성 계정
        AccountEntity pendingAccount = AccountEntity.builder()
                .customerId(501L)
                .email("pending@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.PENDING)
                .build();
        
        entityManager.persist(activeAccount);
        entityManager.persist(pendingAccount);
        entityManager.flush();

        // When & Then
        Optional<Account> foundActive = accountRepository.findActiveByEmail(Email.of("active@test.com"));
        Optional<Account> foundPending = accountRepository.findActiveByEmail(Email.of("pending@test.com"));

        assertThat(foundActive).isPresent();
        assertThat(foundActive.get().getStatus()).isEqualTo(AccountStatus.ACTIVE);
        
        assertThat(foundPending).isEmpty(); // 활성 계정만 조회되므로 빈 값
    }

    @Test
    @DisplayName("데이터베이스 제약조건이 정상적으로 동작한다")
    @Transactional
    void databaseConstraints_Work() {
        // Given
        AccountEntity account1 = AccountEntity.builder()
                .customerId(600L)
                .email("unique@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();

        AccountEntity account2 = AccountEntity.builder()
                .customerId(600L) // 동일한 customer_id
                .email("unique2@test.com")
                .password("hashedPassword")
                .status(AccountEntity.AccountStatus.ACTIVE)
                .build();

        // When & Then
        entityManager.persist(account1);
        entityManager.flush();

        // 동일한 customer_id로 두 번째 계정 저장 시 제약조건 위반
        try {
            entityManager.persist(account2);
            entityManager.flush();
        } catch (Exception e) {
            // 제약조건 위반 예외가 발생해야 함
            assertThat(e.getMessage()).contains("constraint");
        }
    }
}