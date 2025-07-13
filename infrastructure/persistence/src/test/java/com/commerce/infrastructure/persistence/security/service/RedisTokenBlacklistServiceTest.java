package com.commerce.infrastructure.persistence.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisTokenBlacklistService 테스트")
class RedisTokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisTokenBlacklistService redisTokenBlacklistService;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testtoken";
    private static final String BLACKLIST_KEY = "jwt:blacklist:" + TEST_TOKEN;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
    }

    @Test
    @DisplayName("토큰을 블랙리스트에 성공적으로 추가한다")
    void addToBlacklist_Success() {
        // Given
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 3600000L; // 1시간 후

        // When
        redisTokenBlacklistService.addToBlacklist(TEST_TOKEN, expirationTime);

        // Then
        then(valueOperations).should(times(1))
                .set(eq(BLACKLIST_KEY), eq("blacklisted"), any(Duration.class));
    }

    @Test
    @DisplayName("이미 만료된 토큰은 블랙리스트에 추가하지 않는다")
    void addToBlacklist_ExpiredToken() {
        // Given
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime - 3600000L; // 1시간 전

        // When
        redisTokenBlacklistService.addToBlacklist(TEST_TOKEN, expirationTime);

        // Then
        then(valueOperations).should(never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("Redis 오류 시에도 예외가 발생하지 않는다")
    void addToBlacklist_RedisException() {
        // Given
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 3600000L;
        
        doThrow(new RuntimeException("Redis connection failed"))
                .when(valueOperations).set(anyString(), anyString(), any(Duration.class));

        // When & Then (예외가 발생하지 않아야 함)
        redisTokenBlacklistService.addToBlacklist(TEST_TOKEN, expirationTime);

        then(valueOperations).should(times(1))
                .set(eq(BLACKLIST_KEY), eq("blacklisted"), any(Duration.class));
    }

    @Test
    @DisplayName("블랙리스트에 등록된 토큰을 정확히 식별한다")
    void isTokenBlacklisted_True() {
        // Given
        given(redisTemplate.hasKey(BLACKLIST_KEY)).willReturn(true);

        // When
        boolean result = redisTokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);

        // Then
        assertThat(result).isTrue();
        then(redisTemplate).should(times(1)).hasKey(BLACKLIST_KEY);
    }

    @Test
    @DisplayName("블랙리스트에 등록되지 않은 토큰을 정확히 식별한다")
    void isTokenBlacklisted_False() {
        // Given
        given(redisTemplate.hasKey(BLACKLIST_KEY)).willReturn(false);

        // When
        boolean result = redisTokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);

        // Then
        assertThat(result).isFalse();
        then(redisTemplate).should(times(1)).hasKey(BLACKLIST_KEY);
    }

    @Test
    @DisplayName("Redis 조회 오류 시 false를 반환한다 (안전한 정책)")
    void isTokenBlacklisted_RedisException() {
        // Given
        given(redisTemplate.hasKey(BLACKLIST_KEY))
                .willThrow(new RuntimeException("Redis connection failed"));

        // When
        boolean result = redisTokenBlacklistService.isTokenBlacklisted(TEST_TOKEN);

        // Then
        assertThat(result).isFalse(); // 보안상 더 관대한 정책
        then(redisTemplate).should(times(1)).hasKey(BLACKLIST_KEY);
    }

    @Test
    @DisplayName("토큰을 블랙리스트에서 성공적으로 제거한다")
    void removeFromBlacklist_Success() {
        // When
        redisTokenBlacklistService.removeFromBlacklist(TEST_TOKEN);

        // Then
        then(redisTemplate).should(times(1)).delete(BLACKLIST_KEY);
    }

    @Test
    @DisplayName("블랙리스트 크기를 정확히 반환한다")
    void getBlacklistSize_Success() {
        // Given
        Set<String> mockKeys = Set.of(
                "jwt:blacklist:token1",
                "jwt:blacklist:token2",
                "jwt:blacklist:token3"
        );
        given(redisTemplate.keys("jwt:blacklist:*")).willReturn(mockKeys);

        // When
        long result = redisTokenBlacklistService.getBlacklistSize();

        // Then
        assertThat(result).isEqualTo(3L);
        then(redisTemplate).should(times(1)).keys("jwt:blacklist:*");
    }

    @Test
    @DisplayName("블랙리스트 크기 조회 중 오류 시 0을 반환한다")
    void getBlacklistSize_Exception() {
        // Given
        given(redisTemplate.keys("jwt:blacklist:*"))
                .willThrow(new RuntimeException("Redis error"));

        // When
        long result = redisTokenBlacklistService.getBlacklistSize();

        // Then
        assertThat(result).isEqualTo(0L);
        then(redisTemplate).should(times(1)).keys("jwt:blacklist:*");
    }

    @Test
    @DisplayName("TTL 계산이 정확하게 동작한다")
    void addToBlacklist_TTLCalculation() {
        // Given
        long currentTime = System.currentTimeMillis();
        long expirationTime = currentTime + 7200000L; // 2시간 후

        // When
        redisTokenBlacklistService.addToBlacklist(TEST_TOKEN, expirationTime);

        // Then
        then(valueOperations).should(times(1))
                .set(eq(BLACKLIST_KEY), eq("blacklisted"), argThat(duration -> {
                    long expectedTtl = 7200000L; // 2시간
                    long actualTtl = duration.toMillis();
                    return Math.abs(actualTtl - expectedTtl) < 1000; // 1초 오차 허용
                }));
    }
}