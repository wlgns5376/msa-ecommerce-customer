package com.commerce.customer.api.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("integration")
@DisplayName("간단한 통합테스트")
class SimpleIntegrationTest {
    
    @Test
    @DisplayName("스프링 컨텍스트 로딩 테스트")
    void contextLoads() {
        // 스프링 컨텍스트가 정상적으로 로딩되는지 확인
        assertThat(true).isTrue();
    }
}