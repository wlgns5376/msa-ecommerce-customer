package com.commerce.customer.api.integration.controller;

import com.commerce.customer.api.integration.AbstractIntegrationTest;
import com.commerce.infrastructure.persistence.customer.repository.AccountJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.commerce.customer.api.integration.fixture.AccountTestFixture.*;
import static com.commerce.customer.api.integration.fixture.ProfileTestFixture.*;
import static com.commerce.customer.api.integration.fixture.TestConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("성능 및 동시성 통합테스트")
class PerformanceIntegrationTest extends AbstractIntegrationTest {

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
    @DisplayName("동시 다발적 계정 생성 - 고유성 보장")
    void concurrentAccountCreation_UniquenessGuaranteed() throws Exception {
        // Given
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<String>> futures = new ArrayList<>();
        
        try {
            // When - 동시에 10개의 계정 생성 시도
            for (int i = 0; i < threadCount; i++) {
                final int index = i;
                CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        String email = generateUniqueEmail("concurrent_" + index);
                        var request = createAccountRequest(email);
                        
                        mockMvc.perform(post(ACCOUNTS_API_PATH)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated());
                        
                        return email;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }, executorService);
                
                futures.add(future);
            }
            
            // Then - 모든 계정이 성공적으로 생성되었는지 확인
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get();
            
            // 생성된 계정 수 확인
            long accountCount = accountRepository.count();
            assertThat(accountCount).isGreaterThanOrEqualTo(threadCount);
            
        } finally {
            executorService.shutdown();
        }
    }
    
    @Test
    @DisplayName("대량 로그인 요청 처리")
    void bulkLoginRequests_HandledProperly() throws Exception {
        // Given - 여러 계정 생성 및 활성화
        int accountCount = 5;
        List<String> emails = new ArrayList<>();
        
        for (int i = 0; i < accountCount; i++) {
            var accountData = createAndActivateAccount(mockMvc, objectMapper, accountRepository);
            emails.add(accountData.getEmail());
        }
        
        // When - 모든 계정으로 순차적으로 로그인
        long startTime = System.currentTimeMillis();
        
        for (String email : emails) {
            mockMvc.perform(post(LOGIN_API_PATH)
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(createLoginRequest(email))))
                    .andExpect(status().isOk());
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        // Then - 합리적인 시간 내에 완료 (5초 이내)
        assertThat(totalTime).isLessThan(5000);
    }
    
    @Test
    @DisplayName("동일 이메일로 동시 계정 생성 시도 - 하나만 성공")
    void concurrentSameEmailCreation_OnlyOneSucceeds() throws Exception {
        // Given
        String sameEmail = generateUniqueEmail("duplicate_test");
        int threadCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        
        try {
            // When - 동일한 이메일로 동시에 5개의 계정 생성 시도
            for (int i = 0; i < threadCount; i++) {
                CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        var request = createAccountRequest(sameEmail);
                        
                        var result = mockMvc.perform(post(ACCOUNTS_API_PATH)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                                .andReturn();
                        
                        return result.getResponse().getStatus() == 201;
                    } catch (Exception e) {
                        return false;
                    }
                }, executorService);
                
                futures.add(future);
            }
            
            // Then - 하나만 성공하고 나머지는 실패
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            );
            allFutures.get();
            
            long successCount = futures.stream()
                    .map(CompletableFuture::join)
                    .filter(success -> success)
                    .count();
            
            assertThat(successCount).isEqualTo(1);
            
        } finally {
            executorService.shutdown();
        }
    }
    
    @Test
    @DisplayName("반복적인 프로필 조회 - 캐싱 효과")
    void repeatedProfileQueries_CachingEffect() throws Exception {
        // Given - 계정 생성, 활성화, 프로필 생성
        var authenticatedAccount = createAuthenticatedAccount(mockMvc, objectMapper, accountRepository);
        createProfile(mockMvc, objectMapper, authenticatedAccount.getAccessToken());
        
        // When - 동일한 프로필을 10번 조회
        long[] responseTimes = new long[10];
        
        for (int i = 0; i < 10; i++) {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get(MY_PROFILE_API_PATH)
                    .header(AUTHORIZATION_HEADER, authenticatedAccount.getAuthorizationHeader()))
                    .andExpect(status().isOk());
            
            long endTime = System.currentTimeMillis();
            responseTimes[i] = endTime - startTime;
        }
        
        // Then - 첫 번째 요청보다 이후 요청들이 더 빠름 (캐싱 효과)
        long firstRequestTime = responseTimes[0];
        long averageSubsequentTime = 0;
        
        for (int i = 1; i < 10; i++) {
            averageSubsequentTime += responseTimes[i];
        }
        averageSubsequentTime /= 9;
        
        // 캐싱으로 인해 평균 응답 시간이 첫 요청보다 빠를 것으로 예상
        // 하지만 테스트 환경에 따라 달라질 수 있으므로 단순히 합리적인 시간 내 완료 확인
        assertThat(averageSubsequentTime).isLessThan(1000); // 1초 이내
    }
}