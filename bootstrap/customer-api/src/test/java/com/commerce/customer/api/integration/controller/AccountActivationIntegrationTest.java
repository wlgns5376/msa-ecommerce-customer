package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.customer.api.integration.fixture.AccountTestFixture;
import com.commerce.customer.api.integration.fixture.AccountTestFixture.AccountTestData;
import com.commerce.customer.api.integration.helper.MockMvcHelper;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static com.commerce.customer.api.integration.fixture.AccountTestFixture.*;
import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("계정 활성화 플로우 통합테스트")
class AccountActivationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private AccountJpaRepository accountRepository;
    
    @BeforeEach
    void setUp() {
        waitForContainers();
    }
    
    @Test
    @DisplayName("계정 생성 -> 활성화 -> 로그인 -> 로그아웃 전체 시나리오")
    void accountLifecycleTest() throws Exception {
        // Given & When - 계정 생성 및 활성화
        AccountTestData accountData = createAndActivateAccount(mockMvc, objectMapper, accountRepository);
        
        // Then - 로그인
        String accessToken = login(mockMvc, objectMapper, accountData.getEmail());
        
        mockMvc.perform(post(LOGIN_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(createLoginRequest(accountData.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.accountId").value(accountData.getAccountId()))
                .andExpect(jsonPath("$.email").value(accountData.getEmail()));
        
        // 로그아웃
        mockMvc.perform(post(LOGOUT_API_PATH)
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken))
                .andExpect(status().isNoContent());
    }
    
    @Test
    @DisplayName("활성화되지 않은 계정으로 로그인 시도 - 실패")
    void loginWithPendingAccount_Failure() throws Exception {
        // Given - 계정 생성 (활성화하지 않음)
        String email = generateUniqueEmail("pending");
        createAccount(mockMvc, objectMapper, email);
        
        // When & Then - 활성화하지 않은 계정으로 로그인 시도
        mockMvc.perform(post(LOGIN_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(createLoginRequest(email))))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("잘못된 활성화 코드로 계정 활성화 시도 - 실패")
    void activateAccountWithInvalidCode_Failure() throws Exception {
        // Given - 계정 생성
        Long accountId = createAccount(mockMvc, objectMapper, generateUniqueEmail("invalid_code"));
        
        // When & Then - 잘못된 활성화 코드로 활성화 시도
        mockMvc.perform(post(ACTIVATE_API_PATH, accountId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(createActivateRequest("INVALID_CODE"))))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @DisplayName("이미 활성화된 계정을 다시 활성화 시도 - 실패")
    void activateAlreadyActivatedAccount_Failure() throws Exception {
        // Given - 계정 생성 및 활성화
        AccountTestData accountData = createAndActivateAccount(mockMvc, objectMapper, accountRepository);
        
        // When & Then - 두 번째 활성화 시도
        mockMvc.perform(post(ACTIVATE_API_PATH, accountData.getAccountId())
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(createActivateRequest(accountData.getActivationCode()))))
                .andExpect(status().isBadRequest());
    }
}