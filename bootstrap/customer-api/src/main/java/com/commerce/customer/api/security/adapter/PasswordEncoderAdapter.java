package com.commerce.customer.api.security.adapter;

import com.commerce.customer.core.domain.service.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 도메인 계층의 PasswordEncoder 인터페이스를 구현하는 어댑터
 * Spring Security의 BCryptPasswordEncoder를 사용
 */
@Component
@RequiredArgsConstructor
public class PasswordEncoderAdapter implements PasswordEncoder {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public String encode(String rawPassword) {
        return bCryptPasswordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return bCryptPasswordEncoder.matches(rawPassword, encodedPassword);
    }
}