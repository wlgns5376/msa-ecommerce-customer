-- 계정 활성화 코드 필드 추가
ALTER TABLE accounts ADD COLUMN activation_code VARCHAR(32);
ALTER TABLE accounts ADD COLUMN activation_code_expires_at TIMESTAMP;

-- 인덱스 추가 (활성화 코드 조회 성능 향상)
CREATE INDEX idx_account_activation_code ON accounts(activation_code) WHERE activation_code IS NOT NULL;