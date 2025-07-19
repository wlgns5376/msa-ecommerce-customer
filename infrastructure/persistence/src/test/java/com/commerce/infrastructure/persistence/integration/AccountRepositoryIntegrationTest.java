package com.commerce.infrastructure.persistence.integration;

import com.commerce.customer.core.domain.model.*;
import com.commerce.customer.core.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest
@ActiveProfiles("integration")
@Testcontainers
@DisplayName("AccountRepository 통합 테스트")
@Transactional
class AccountRepositoryIntegrationTest {

    @Container
    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>(parse("mariadb:11.1"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(false)
            .withStartupTimeoutSeconds(120);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
        registry.add("spring.datasource.driver-class-name", mariaDB::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MariaDBDialect");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        // TestContainers 시작 대기
        mariaDB.isRunning();
    }

    @Test
    @DisplayName("컨테이너가 정상적으로 시작되는지 확인")
    void containerStarts_Success() {
        // Given - 컨테이너 상태 확인
        mariaDB.isRunning();
        
        // When & Then - 단순히 테스트가 실행되면 성공
        assertThat(mariaDB.isRunning()).isTrue();
    }

    @Test  
    @DisplayName("TestContainers MariaDB 연결 확인")
    void mariaDbConnection_Success() {
        // Given & When - 데이터베이스 연결 확인
        String jdbcUrl = mariaDB.getJdbcUrl();
        String username = mariaDB.getUsername();
        String password = mariaDB.getPassword();
        
        // Then
        assertThat(jdbcUrl).contains("mariadb");
        assertThat(username).isEqualTo("test");
        assertThat(password).isEqualTo("test");
    }
    
    @Test
    @DisplayName("계정 저장 및 조회 - 성공")
    void saveAndFindAccount_Success() {
        // Given
        Email email = Email.of("test@example.com");
        Password password = Password.of("Password123!");
        CustomerId customerId = accountRepository.generateCustomerId();
        
        Account account = Account.create(customerId, email, password);
        
        // When
        Account savedAccount = accountRepository.save(account);
        
        // Then
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getEmail()).isEqualTo(email);
        
        // 조회 테스트
        Optional<Account> foundAccount = accountRepository.findByEmail(email);
        assertThat(foundAccount).isPresent();
        assertThat(foundAccount.get().getEmail()).isEqualTo(email);
    }
    
    @Test
    @DisplayName("이메일 중복 확인 - 성공")
    void existsByEmail_Success() {
        // Given
        Email email = Email.of("duplicate@example.com");
        Password password = Password.of("Password123!");
        CustomerId customerId = accountRepository.generateCustomerId();
        
        Account account = Account.create(customerId, email, password);
        accountRepository.save(account);
        
        // When & Then
        assertThat(accountRepository.existsByEmail(email)).isTrue();
        assertThat(accountRepository.existsByEmail(Email.of("notexist@example.com"))).isFalse();
    }
}