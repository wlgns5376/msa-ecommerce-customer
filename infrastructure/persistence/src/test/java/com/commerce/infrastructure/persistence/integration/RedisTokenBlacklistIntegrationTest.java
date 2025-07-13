package com.commerce.infrastructure.persistence.integration;

import com.commerce.infrastructure.persistence.config.RedisConfig;
import com.commerce.infrastructure.persistence.security.service.RedisTokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = {RedisConfig.class, RedisTokenBlacklistService.class})
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Redis Token Blacklist 통합 테스트")
@org.junit.jupiter.api.Disabled("Docker/Testcontainers 환경 문제로 임시 비활성화")
class RedisTokenBlacklistIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private RedisTokenBlacklistService tokenBlacklistService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testtoken";

    @BeforeEach
    void setUp() {
        // Redis 정리
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("토큰을 블랙리스트에 추가하고 조회할 수 있다")
    void addAndCheckBlacklist_Success() {
        // Given
        long expirationTime = System.currentTimeMillis() + 60000; // 1분 후

        // When
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expirationTime);

        // Then
        assertThat(tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN)).isTrue();
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(1L);
    }

    @Test
    @DisplayName("블랙리스트에 없는 토큰은 false를 반환한다")
    void checkNonBlacklistedToken_False() {
        // Given
        String nonBlacklistedToken = "nonblacklisted.token";

        // When & Then
        assertThat(tokenBlacklistService.isTokenBlacklisted(nonBlacklistedToken)).isFalse();
    }

    @Test
    @DisplayName("토큰을 블랙리스트에서 제거할 수 있다")
    void removeFromBlacklist_Success() {
        // Given
        long expirationTime = System.currentTimeMillis() + 60000;
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, expirationTime);
        assertThat(tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN)).isTrue();

        // When
        tokenBlacklistService.removeFromBlacklist(TEST_TOKEN);

        // Then
        assertThat(tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN)).isFalse();
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(0L);
    }

    @Test
    @DisplayName("TTL이 설정된 토큰은 만료 후 자동으로 제거된다")
    void tokenExpiration_AutoRemoval() {
        // Given
        long shortExpirationTime = System.currentTimeMillis() + 1000; // 1초 후
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, shortExpirationTime);

        // When - 토큰이 추가된 직후에는 블랙리스트에 있어야 함
        assertThat(tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN)).isTrue();

        // Then - 2초 후에는 TTL에 의해 자동 제거되어야 함
        await().atMost(Duration.ofSeconds(3))
                .untilAsserted(() -> {
                    assertThat(tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN)).isFalse();
                    assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(0L);
                });
    }

    @Test
    @DisplayName("여러 토큰을 블랙리스트에 관리할 수 있다")
    void multipleTokens_Management() {
        // Given
        String token1 = "token1.test";
        String token2 = "token2.test";
        String token3 = "token3.test";
        long expirationTime = System.currentTimeMillis() + 60000;

        // When
        tokenBlacklistService.addToBlacklist(token1, expirationTime);
        tokenBlacklistService.addToBlacklist(token2, expirationTime);
        tokenBlacklistService.addToBlacklist(token3, expirationTime);

        // Then
        assertThat(tokenBlacklistService.isTokenBlacklisted(token1)).isTrue();
        assertThat(tokenBlacklistService.isTokenBlacklisted(token2)).isTrue();
        assertThat(tokenBlacklistService.isTokenBlacklisted(token3)).isTrue();
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(3L);

        // 하나씩 제거
        tokenBlacklistService.removeFromBlacklist(token2);
        assertThat(tokenBlacklistService.isTokenBlacklisted(token2)).isFalse();
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(2L);
    }

    @Test
    @DisplayName("이미 만료된 토큰은 블랙리스트에 추가되지 않는다")
    void expiredToken_NotAdded() {
        // Given
        long pastExpirationTime = System.currentTimeMillis() - 60000; // 1분 전

        // When
        tokenBlacklistService.addToBlacklist(TEST_TOKEN, pastExpirationTime);

        // Then
        assertThat(tokenBlacklistService.isTokenBlacklisted(TEST_TOKEN)).isFalse();
        assertThat(tokenBlacklistService.getBlacklistSize()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Redis 연결이 정상적으로 동작한다")
    void redisConnection_Works() {
        // Given
        String testKey = "test:key";
        String testValue = "test:value";

        // When
        redisTemplate.opsForValue().set(testKey, testValue);

        // Then
        String retrievedValue = redisTemplate.opsForValue().get(testKey);
        assertThat(retrievedValue).isEqualTo(testValue);

        // Cleanup
        redisTemplate.delete(testKey);
    }
}