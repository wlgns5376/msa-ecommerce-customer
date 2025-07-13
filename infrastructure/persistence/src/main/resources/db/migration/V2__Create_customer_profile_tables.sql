-- =====================================================
-- Customer Profile 관련 테이블들 생성
-- =====================================================

-- Customer Profile 테이블
CREATE TABLE customer_profiles (
    profile_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    
    -- Personal Info
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    birth_date DATE NULL,
    gender VARCHAR(10) NULL,
    profile_image_url VARCHAR(500) NULL,
    
    -- Contact Info
    primary_phone VARCHAR(20) NOT NULL,
    secondary_phone VARCHAR(20) NULL,
    
    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    
    -- Marketing Consent
    email_marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    sms_marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    push_marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Notification Settings
    order_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    promotion_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    account_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    review_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- 공통 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM'
);

-- Address 테이블
CREATE TABLE addresses (
    address_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    alias VARCHAR(50) NULL,
    zip_code VARCHAR(10) NOT NULL,
    road_address VARCHAR(200) NOT NULL,
    jibun_address VARCHAR(200) NULL,
    detail_address VARCHAR(100) NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- 공통 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM',
    
    -- 외래키
    FOREIGN KEY (profile_id) REFERENCES customer_profiles(profile_id) ON DELETE CASCADE
);

-- Brand Preference 테이블
CREATE TABLE brand_preferences (
    brand_preference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    brand_name VARCHAR(100) NOT NULL,
    preference_level VARCHAR(20) NOT NULL,
    
    -- 공통 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM',
    
    -- 외래키
    FOREIGN KEY (profile_id) REFERENCES customer_profiles(profile_id) ON DELETE CASCADE,
    
    -- 유니크 제약
    UNIQUE KEY uk_profile_brand (profile_id, brand_name)
);

-- Category Interest 테이블
CREATE TABLE category_interests (
    category_interest_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    category_name VARCHAR(100) NOT NULL,
    interest_level VARCHAR(20) NOT NULL,
    
    -- 공통 필드
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM',
    
    -- 외래키
    FOREIGN KEY (profile_id) REFERENCES customer_profiles(profile_id) ON DELETE CASCADE,
    
    -- 유니크 제약
    UNIQUE KEY uk_profile_category (profile_id, category_name)
);

-- =====================================================
-- 인덱스 생성
-- =====================================================

-- Customer Profile 인덱스
CREATE INDEX idx_profile_customer_id ON customer_profiles(customer_id);
CREATE INDEX idx_profile_status ON customer_profiles(status);
CREATE INDEX idx_profile_created_at ON customer_profiles(created_at);

-- Address 인덱스
CREATE INDEX idx_address_profile_id ON addresses(profile_id);
CREATE INDEX idx_address_type ON addresses(type);
CREATE INDEX idx_address_default ON addresses(is_default);

-- Brand Preference 인덱스
CREATE INDEX idx_brand_preference_profile_id ON brand_preferences(profile_id);
CREATE INDEX idx_brand_preference_brand_name ON brand_preferences(brand_name);

-- Category Interest 인덱스
CREATE INDEX idx_category_interest_profile_id ON category_interests(profile_id);
CREATE INDEX idx_category_interest_category_name ON category_interests(category_name);

-- =====================================================
-- 제약 조건 추가
-- =====================================================

-- Customer Profile 제약 조건
ALTER TABLE customer_profiles 
ADD CONSTRAINT chk_profile_status 
CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));

ALTER TABLE customer_profiles 
ADD CONSTRAINT chk_profile_gender 
CHECK (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL);

-- Address 제약 조건
ALTER TABLE addresses 
ADD CONSTRAINT chk_address_type 
CHECK (type IN ('HOME', 'WORK', 'OTHER'));

-- Brand Preference 제약 조건
ALTER TABLE brand_preferences 
ADD CONSTRAINT chk_brand_preference_level 
CHECK (preference_level IN ('LOVE', 'LIKE', 'DISLIKE'));

-- Category Interest 제약 조건
ALTER TABLE category_interests 
ADD CONSTRAINT chk_category_interest_level 
CHECK (interest_level IN ('HIGH', 'MEDIUM', 'LOW'));

-- =====================================================
-- 테이블 코멘트 추가
-- =====================================================

ALTER TABLE customer_profiles COMMENT = '고객 프로필 정보';
ALTER TABLE addresses COMMENT = '고객 주소 정보';
ALTER TABLE brand_preferences COMMENT = '고객 브랜드 선호도';
ALTER TABLE category_interests COMMENT = '고객 카테고리 관심도';