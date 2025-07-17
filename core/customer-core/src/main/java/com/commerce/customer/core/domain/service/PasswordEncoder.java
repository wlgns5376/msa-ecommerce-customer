package com.commerce.customer.core.domain.service;

/**
 * 비밀번호 인코더 인터페이스
 * 도메인 계층에서 사용하는 비밀번호 암호화 인터페이스
 */
public interface PasswordEncoder {
    
    /**
     * 원본 비밀번호를 암호화합니다.
     * 
     * @param rawPassword 원본 비밀번호
     * @return 암호화된 비밀번호
     */
    String encode(String rawPassword);
    
    /**
     * 원본 비밀번호와 암호화된 비밀번호가 일치하는지 검증합니다.
     * 
     * @param rawPassword 원본 비밀번호
     * @param encodedPassword 암호화된 비밀번호
     * @return 일치하면 true, 아니면 false
     */
    boolean matches(String rawPassword, String encodedPassword);
}