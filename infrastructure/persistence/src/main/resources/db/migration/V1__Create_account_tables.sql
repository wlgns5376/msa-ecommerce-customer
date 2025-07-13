-- =====================================================
-- Account 테이블 생성
-- =====================================================

CREATE TABLE accounts (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    activated_at TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(100) DEFAULT 'SYSTEM',
    updated_by VARCHAR(100) DEFAULT 'SYSTEM'
);

-- 인덱스 생성
CREATE INDEX idx_account_email ON accounts(email);
CREATE INDEX idx_account_customer_id ON accounts(customer_id);
CREATE INDEX idx_account_status ON accounts(status);
CREATE INDEX idx_account_created_at ON accounts(created_at);

-- 제약 조건 추가
ALTER TABLE accounts 
ADD CONSTRAINT chk_account_status 
CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'DEACTIVATED'));

-- 코멘트 추가
ALTER TABLE accounts COMMENT = '고객 계정 정보';
ALTER TABLE accounts MODIFY COLUMN account_id BIGINT AUTO_INCREMENT COMMENT '계정 ID (PK)';
ALTER TABLE accounts MODIFY COLUMN customer_id BIGINT NOT NULL COMMENT '고객 ID (UK)';
ALTER TABLE accounts MODIFY COLUMN email VARCHAR(100) NOT NULL COMMENT '이메일 주소 (UK)';
ALTER TABLE accounts MODIFY COLUMN password VARCHAR(255) NOT NULL COMMENT '암호화된 비밀번호';
ALTER TABLE accounts MODIFY COLUMN status VARCHAR(20) NOT NULL COMMENT '계정 상태 (PENDING/ACTIVE/SUSPENDED/DEACTIVATED)';
ALTER TABLE accounts MODIFY COLUMN activated_at TIMESTAMP NULL COMMENT '계정 활성화 일시';
ALTER TABLE accounts MODIFY COLUMN last_login_at TIMESTAMP NULL COMMENT '마지막 로그인 일시';