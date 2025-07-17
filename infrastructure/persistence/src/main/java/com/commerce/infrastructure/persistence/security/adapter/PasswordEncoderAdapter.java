package com.commerce.infrastructure.persistence.security.adapter;

import com.commerce.customer.core.domain.service.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Spring Security의 PasswordEncoder를 도메인 계층의 PasswordEncoder로 변환하는 어댑터
 */
@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoder {
    
    private final org.springframework.security.crypto.password.PasswordEncoder springPasswordEncoder;
    
    @Override
    public String encode(String rawPassword) {
        return springPasswordEncoder.encode(rawPassword);
    }
    
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return springPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}