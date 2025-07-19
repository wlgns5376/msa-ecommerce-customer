package com.commerce.customer.api.controller;

import com.commerce.customer.api.dto.account.ActivateAccountRequest;
import com.commerce.customer.core.application.service.AccountApplicationService;
import com.commerce.customer.core.application.usecase.account.ActivateAccountUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerActivationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AccountApplicationService accountApplicationService;

    @Test
    void 계정_활성화_성공() throws Exception {
        // given
        Long accountId = 1L;
        String activationCode = "12345678901234567890123456789012";
        ActivateAccountRequest request = new ActivateAccountRequest(activationCode);
        
        doNothing().when(accountApplicationService).activateAccount(any(ActivateAccountUseCase.class));

        // when & then
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("계정이 성공적으로 활성화되었습니다."))
                .andExpect(jsonPath("$.activated").value(true));

        verify(accountApplicationService, times(1)).activateAccount(any(ActivateAccountUseCase.class));
    }

    @Test
    void 빈_인증코드로_활성화시_400_에러() throws Exception {
        // given
        Long accountId = 1L;
        ActivateAccountRequest request = new ActivateAccountRequest("");

        // when & then
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 계정이_없을때_활성화시_예외발생() throws Exception {
        // given
        Long accountId = 999L;
        String activationCode = "12345678901234567890123456789012";
        ActivateAccountRequest request = new ActivateAccountRequest(activationCode);
        
        doThrow(new IllegalArgumentException("계정을 찾을 수 없습니다."))
            .when(accountApplicationService).activateAccount(any(ActivateAccountUseCase.class));

        // when & then
        mockMvc.perform(post("/api/v1/accounts/{accountId}/activate", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}