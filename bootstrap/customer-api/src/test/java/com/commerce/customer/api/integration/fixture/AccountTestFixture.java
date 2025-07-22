package com.commerce.customer.api.integration.fixture;

import com.commerce.customer.api.dto.account.ActivateAccountRequest;
import com.commerce.customer.api.dto.account.CreateAccountRequest;
import com.commerce.customer.api.dto.account.LoginRequest;
import com.commerce.infrastructure.persistence.customer.entity.AccountEntity;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 계정 관련 테스트 Fixture Factory
 */
public class AccountTestFixture {
    
    /**
     * 고유한 이메일 생성
     */
    public static String generateUniqueEmail() {
        return generateUniqueEmail("test");
    }
    
    /**
     * 접두사를 포함한 고유한 이메일 생성
     */
    public static String generateUniqueEmail(String prefix) {
        return prefix + "_" + System.currentTimeMillis() + "@example.com";
    }
    
    /**
     * 계정 생성 요청 DTO 생성
     */
    public static CreateAccountRequest createAccountRequest() {
        return createAccountRequest(generateUniqueEmail());
    }
    
    /**
     * 특정 이메일로 계정 생성 요청 DTO 생성
     */
    public static CreateAccountRequest createAccountRequest(String email) {
        return new CreateAccountRequest(email, TEST_PASSWORD);
    }
    
    /**
     * 로그인 요청 DTO 생성
     */
    public static LoginRequest createLoginRequest(String email) {
        return new LoginRequest(email, TEST_PASSWORD);
    }
    
    /**
     * 계정 활성화 요청 DTO 생성
     */
    public static ActivateAccountRequest createActivateRequest(String activationCode) {
        return new ActivateAccountRequest(activationCode);
    }
    
    /**
     * 계정 생성 및 ID 반환
     */
    public static Long createAccount(MockMvc mockMvc, ObjectMapper objectMapper) throws Exception {
        return createAccount(mockMvc, objectMapper, generateUniqueEmail());
    }
    
    /**
     * 특정 이메일로 계정 생성 및 ID 반환
     */
    public static Long createAccount(MockMvc mockMvc, ObjectMapper objectMapper, String email) throws Exception {
        CreateAccountRequest request = createAccountRequest(email);
        
        MvcResult result = mockMvc.perform(post(ACCOUNTS_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accountId").asLong();
    }
    
    /**
     * 계정 활성화
     */
    public static void activateAccount(MockMvc mockMvc, ObjectMapper objectMapper, 
                                     Long accountId, String activationCode) throws Exception {
        ActivateAccountRequest request = createActivateRequest(activationCode);
        
        mockMvc.perform(post(ACTIVATE_API_PATH, accountId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    
    /**
     * 계정 생성 후 활성화까지 완료
     */
    public static AccountTestData createAndActivateAccount(MockMvc mockMvc, ObjectMapper objectMapper,
                                                          AccountJpaRepository accountRepository) throws Exception {
        String email = generateUniqueEmail();
        Long accountId = createAccount(mockMvc, objectMapper, email);
        
        // 활성화 코드 조회
        AccountEntity entity = accountRepository.findById(accountId)
                .orElseThrow(() -> new AssertionError("계정을 찾을 수 없습니다."));
        String activationCode = entity.getActivationCode();
        
        // 계정 활성화
        activateAccount(mockMvc, objectMapper, accountId, activationCode);
        
        return new AccountTestData(accountId, email, activationCode);
    }
    
    /**
     * 로그인 후 액세스 토큰 반환
     */
    public static String login(MockMvc mockMvc, ObjectMapper objectMapper, String email) throws Exception {
        LoginRequest request = createLoginRequest(email);
        
        MvcResult result = mockMvc.perform(post(LOGIN_API_PATH)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();
        
        String response = result.getResponse().getContentAsString();
        return objectMapper.readTree(response).get("accessToken").asText();
    }
    
    /**
     * 계정 생성, 활성화, 로그인까지 완료 후 데이터 반환
     */
    public static AuthenticatedAccountData createAuthenticatedAccount(MockMvc mockMvc, ObjectMapper objectMapper,
                                                                     AccountJpaRepository accountRepository) throws Exception {
        AccountTestData accountData = createAndActivateAccount(mockMvc, objectMapper, accountRepository);
        String accessToken = login(mockMvc, objectMapper, accountData.getEmail());
        
        return new AuthenticatedAccountData(
            accountData.getAccountId(),
            accountData.getEmail(),
            accountData.getActivationCode(),
            accessToken
        );
    }
    
    /**
     * 계정 테스트 데이터 클래스
     */
    public static class AccountTestData {
        private final Long accountId;
        private final String email;
        private final String activationCode;
        
        public AccountTestData(Long accountId, String email, String activationCode) {
            this.accountId = accountId;
            this.email = email;
            this.activationCode = activationCode;
        }
        
        public Long getAccountId() { return accountId; }
        public String getEmail() { return email; }
        public String getActivationCode() { return activationCode; }
    }
    
    /**
     * 인증된 계정 테스트 데이터 클래스
     */
    public static class AuthenticatedAccountData extends AccountTestData {
        private final String accessToken;
        
        public AuthenticatedAccountData(Long accountId, String email, String activationCode, String accessToken) {
            super(accountId, email, activationCode);
            this.accessToken = accessToken;
        }
        
        public String getAccessToken() { return accessToken; }
        public String getAuthorizationHeader() { return BEARER_PREFIX + accessToken; }
    }
}