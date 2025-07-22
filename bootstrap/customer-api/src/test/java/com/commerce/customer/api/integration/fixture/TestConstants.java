package com.commerce.customer.api.integration.fixture;

import java.time.LocalDate;

/**
 * 통합 테스트에서 사용되는 공통 상수들
 */
public class TestConstants {
    
    // 계정 관련 상수
    public static final String TEST_PASSWORD = "Password123!";
    public static final String WEAK_PASSWORD = "weak";
    public static final String INVALID_EMAIL = "invalid-email";
    
    // 프로필 관련 상수
    public static final String TEST_FIRST_NAME = "홍";
    public static final String TEST_LAST_NAME = "길동";
    public static final String TEST_PHONE = "010-1234-5678";
    public static final String TEST_PHONE_UPDATED = "010-9876-5432";
    public static final LocalDate TEST_BIRTH_DATE = LocalDate.of(1990, 1, 1);
    public static final String TEST_GENDER = "MALE";
    
    // 주소 관련 상수
    public static final String TEST_ZIPCODE = "12345";
    public static final String TEST_ROAD_ADDRESS = "서울특별시 강남구 테헤란로 123";
    public static final String TEST_JIBUN_ADDRESS = "서울특별시 강남구 대치동 123-45";
    public static final String TEST_DETAIL_ADDRESS = "타워빌딩 10층";
    
    // API 경로 상수
    public static final String ACCOUNTS_API_PATH = "/api/v1/accounts";
    public static final String PROFILES_API_PATH = "/api/v1/profiles";
    public static final String LOGIN_API_PATH = "/api/v1/accounts/login";
    public static final String LOGOUT_API_PATH = "/api/v1/accounts/logout";
    public static final String ACTIVATE_API_PATH = "/api/v1/accounts/{accountId}/activate";
    public static final String MY_PROFILE_API_PATH = "/api/v1/profiles/me";
    
    // 응답 메시지 상수
    public static final String ACCOUNT_CREATED_MESSAGE = "계정이 성공적으로 생성되었습니다.";
    public static final String PROFILE_CREATED_MESSAGE = "프로필이 성공적으로 생성되었습니다.";
    public static final String PROFILE_UPDATED_MESSAGE = "프로필이 성공적으로 수정되었습니다.";
    
    // 헤더 상수
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    
    private TestConstants() {
        // 인스턴스화 방지
    }
}