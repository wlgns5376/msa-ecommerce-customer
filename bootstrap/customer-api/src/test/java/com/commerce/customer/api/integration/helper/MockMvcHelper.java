package com.commerce.customer.api.integration.helper;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static com.commerce.customer.api.integration.fixture.TestConstants.AUTHORIZATION_HEADER;
import static com.commerce.customer.api.integration.fixture.TestConstants.BEARER_PREFIX;

/**
 * MockMvc 요청을 위한 헬퍼 클래스
 */
public class MockMvcHelper {
    
    /**
     * JSON 요청 빌더 생성
     */
    public static MockHttpServletRequestBuilder jsonRequest(MockHttpServletRequestBuilder builder, String content) {
        return builder
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
    }
    
    /**
     * 인증 토큰을 포함한 JSON 요청 빌더 생성
     */
    public static MockHttpServletRequestBuilder authenticatedJsonRequest(MockHttpServletRequestBuilder builder, 
                                                                       String content, String accessToken) {
        return builder
                .header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(content);
    }
    
    /**
     * 인증 토큰을 포함한 요청 빌더 생성
     */
    public static MockHttpServletRequestBuilder authenticatedRequest(MockHttpServletRequestBuilder builder, 
                                                                   String accessToken) {
        return builder.header(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);
    }
    
    /**
     * 응답 본문 추출
     */
    public static String extractResponseBody(ResultActions resultActions) throws Exception {
        return resultActions.andReturn().getResponse().getContentAsString();
    }
    
    /**
     * JSON 응답에서 특정 필드 값 추출
     */
    public static String extractJsonField(ResultActions resultActions, String fieldName) throws Exception {
        String response = extractResponseBody(resultActions);
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response)
                .get(fieldName)
                .asText();
    }
    
    /**
     * JSON 응답에서 Long 타입 필드 값 추출
     */
    public static Long extractJsonLongField(ResultActions resultActions, String fieldName) throws Exception {
        String response = extractResponseBody(resultActions);
        return new com.fasterxml.jackson.databind.ObjectMapper()
                .readTree(response)
                .get(fieldName)
                .asLong();
    }
}