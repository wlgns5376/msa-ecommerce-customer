package com.commerce.customer.api.integration;

import com.commerce.customer.api.config.TestKafkaConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

import static org.testcontainers.utility.DockerImageName.parse;

/**
 * 통합테스트를 위한 추상 기본 클래스
 * TestContainers를 사용하여 MariaDB와 Redis 컨테이너를 제공
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("integration")
@Testcontainers
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@Import(TestKafkaConfig.class)
public abstract class AbstractIntegrationTest {

    @Container
    static final MariaDBContainer<?> mariaDB = new MariaDBContainer<>(parse("mariadb:11.1"))
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withReuse(true)
            .withStartupTimeoutSeconds(120)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(120)));

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)));

    @BeforeAll
    static void initializeContainers() {
        // 컨테이너 명시적 시작 및 대기
        if (!mariaDB.isRunning()) {
            mariaDB.start();
        }
        if (!redis.isRunning()) {
            redis.start();
        }
        
        // 컨테이너 완전 시작 대기
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Bean 중복 허용
        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
        
        // MariaDB 설정
        registry.add("spring.datasource.url", mariaDB::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDB::getUsername);
        registry.add("spring.datasource.password", mariaDB::getPassword);
        registry.add("spring.datasource.driver-class-name", mariaDB::getDriverClassName);
        
        // Redis 설정
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
        
        // JPA 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MariaDBDialect");
        
        // Flyway 비활성화
        registry.add("spring.flyway.enabled", () -> "false");
        
        // Kafka 비활성화
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.producer.enabled", () -> "false");
        registry.add("spring.kafka.consumer.enabled", () -> "false");
        
        // 이벤트 발행 비활성화
        registry.add("spring.application.events.enabled", () -> "false");
        
        // Eureka 비활성화
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("spring.cloud.discovery.enabled", () -> "false");
    }

    protected void waitForContainers() {
        // 컨테이너 상태 확인만 수행 (BeforeAll에서 이미 시작됨)
        if (!mariaDB.isRunning() || !redis.isRunning()) {
            throw new IllegalStateException("컨테이너가 시작되지 않았습니다.");
        }
    }
}