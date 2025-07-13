package com.commerce.infrastructure.persistence.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisTokenBlacklistService {

    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 토큰을 블랙리스트에 추가
     * 
     * @param token JWT 토큰
     * @param expirationTime 토큰 만료 시간 (밀리초)
     */
    public void addToBlacklist(String token, long expirationTime) {
        try {
            String key = BLACKLIST_KEY_PREFIX + token;
            
            // 현재 시간부터 토큰 만료 시간까지의 TTL 계산
            long currentTime = System.currentTimeMillis();
            long ttl = Math.max(0, expirationTime - currentTime);
            
            if (ttl > 0) {
                redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofMillis(ttl));
                log.info("Token added to blacklist with TTL: {} ms", ttl);
            } else {
                log.warn("Token already expired, not adding to blacklist");
            }
        } catch (Exception e) {
            log.error("Failed to add token to blacklist: {}", e.getMessage());
            // Redis 오류로 인해 전체 시스템이 중단되지 않도록 예외를 잡습니다
        }
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * 
     * @param token JWT 토큰
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = BLACKLIST_KEY_PREFIX + token;
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check token blacklist status: {}", e.getMessage());
            // Redis 오류 시 false 반환 (보안상 더 관대한 정책)
            return false;
        }
    }

    /**
     * 토큰을 블랙리스트에서 제거 (테스트 목적)
     * 
     * @param token JWT 토큰
     */
    public void removeFromBlacklist(String token) {
        try {
            String key = BLACKLIST_KEY_PREFIX + token;
            redisTemplate.delete(key);
            log.info("Token removed from blacklist");
        } catch (Exception e) {
            log.error("Failed to remove token from blacklist: {}", e.getMessage());
        }
    }

    /**
     * 만료된 블랙리스트 토큰들을 정리 (Redis TTL에 의해 자동으로 정리되므로 주로 모니터링 목적)
     */
    public long getBlacklistSize() {
        try {
            return redisTemplate.keys(BLACKLIST_KEY_PREFIX + "*").size();
        } catch (Exception e) {
            log.error("Failed to get blacklist size: {}", e.getMessage());
            return 0;
        }
    }
}