package com.commerce.customer.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SwaggerConfig 단위 테스트")
class SwaggerConfigTest {

    @InjectMocks
    private SwaggerConfig swaggerConfig;

    private OpenAPI openAPI;

    @BeforeEach
    void setUp() {
        // Given - SwaggerConfig의 openAPI 메서드를 호출하여 OpenAPI 인스턴스 생성
        openAPI = swaggerConfig.openAPI();
    }

    @Test
    @DisplayName("OpenAPI 인스턴스가 정상적으로 생성되어야 한다")
    void shouldCreateOpenAPIInstance() {
        // Then
        assertThat(openAPI).isNotNull();
    }

    @Test
    @DisplayName("API 정보가 올바르게 설정되어야 한다")
    void shouldSetCorrectApiInfo() {
        // When
        Info info = openAPI.getInfo();

        // Then
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Customer Service API");
        assertThat(info.getVersion()).isEqualTo("v1.0");
        assertThat(info.getDescription()).isEqualTo("커머스 플랫폼의 고객 서비스 API 문서입니다.");
    }

    @Test
    @DisplayName("연락처 정보가 올바르게 설정되어야 한다")
    void shouldSetCorrectContactInfo() {
        // When
        Contact contact = openAPI.getInfo().getContact();

        // Then
        assertThat(contact).isNotNull();
        assertThat(contact.getName()).isEqualTo("Customer Service Team");
        assertThat(contact.getEmail()).isEqualTo("customer-service@commerce.com");
    }

    @Test
    @DisplayName("서버 정보가 올바르게 설정되어야 한다")
    void shouldSetCorrectServerInfo() {
        // When
        List<Server> servers = openAPI.getServers();

        // Then
        assertThat(servers).hasSize(3);
        
        // 로컬 서버 검증
        Server localServer = servers.get(0);
        assertThat(localServer.getUrl()).isEqualTo("http://localhost:8081");
        assertThat(localServer.getDescription()).isEqualTo("Local server");
        
        // 개발 서버 검증
        Server devServer = servers.get(1);
        assertThat(devServer.getUrl()).isEqualTo("https://dev-api.commerce.com");
        assertThat(devServer.getDescription()).isEqualTo("Development server");
        
        // 프로덕션 서버 검증
        Server prodServer = servers.get(2);
        assertThat(prodServer.getUrl()).isEqualTo("https://api.commerce.com");
        assertThat(prodServer.getDescription()).isEqualTo("Production server");
    }

    @Test
    @DisplayName("JWT 보안 스킴이 올바르게 설정되어야 한다")
    void shouldSetCorrectJwtSecurityScheme() {
        // When
        Components components = openAPI.getComponents();
        SecurityScheme securityScheme = components.getSecuritySchemes().get("bearerAuth");

        // Then
        assertThat(components).isNotNull();
        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
        assertThat(securityScheme.getIn()).isEqualTo(SecurityScheme.In.HEADER);
        assertThat(securityScheme.getName()).isEqualTo("Authorization");
    }

    @Test
    @DisplayName("전역 보안 요구사항이 올바르게 설정되어야 한다")
    void shouldSetGlobalSecurityRequirement() {
        // When
        List<SecurityRequirement> security = openAPI.getSecurity();

        // Then
        assertThat(security).hasSize(1);
        
        SecurityRequirement requirement = security.get(0);
        assertThat(requirement.keySet()).containsExactly("bearerAuth");
        assertThat(requirement.get("bearerAuth")).isEmpty(); // 빈 리스트는 모든 스코프를 의미
    }

    @Test
    @DisplayName("OpenAPI 버전이 올바르게 설정되어야 한다")
    void shouldSetCorrectOpenApiVersion() {
        // When
        String openApiVersion = openAPI.getOpenapi();

        // Then
        assertThat(openApiVersion).isEqualTo("3.0.3");
    }

    @Test
    @DisplayName("Components에 보안 스킴만 포함되어야 한다")
    void shouldOnlyIncludeSecuritySchemesInComponents() {
        // When
        Components components = openAPI.getComponents();

        // Then
        assertThat(components.getSecuritySchemes()).hasSize(1);
        assertThat(components.getSchemas()).isNullOrEmpty();
        assertThat(components.getResponses()).isNullOrEmpty();
        assertThat(components.getParameters()).isNullOrEmpty();
        assertThat(components.getExamples()).isNullOrEmpty();
        assertThat(components.getRequestBodies()).isNullOrEmpty();
        assertThat(components.getHeaders()).isNullOrEmpty();
        assertThat(components.getLinks()).isNullOrEmpty();
        assertThat(components.getCallbacks()).isNullOrEmpty();
    }

    @Test
    @DisplayName("SwaggerConfig 클래스가 빈으로 등록 가능해야 한다")
    void shouldBeAbleToCreateAsBean() {
        // When
        SwaggerConfig config = new SwaggerConfig();
        OpenAPI createdApi = config.openAPI();

        // Then
        assertThat(config).isNotNull();
        assertThat(createdApi).isNotNull();
        assertThat(createdApi.getInfo().getTitle()).isEqualTo("Customer Service API");
    }
}