-- Add soft delete columns to accounts table
ALTER TABLE accounts 
ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN deleted_at TIMESTAMP NULL;

-- Create index for soft delete
CREATE INDEX idx_account_deleted ON accounts(deleted);

-- Update existing unique indexes to include deleted column
DROP INDEX IF EXISTS idx_account_email;
DROP INDEX IF EXISTS idx_account_customer_id;

CREATE UNIQUE INDEX idx_account_email ON accounts(email, deleted);
CREATE UNIQUE INDEX idx_account_customer_id ON accounts(customer_id, deleted);

-- Add comment for documentation
COMMENT ON COLUMN accounts.deleted IS 'Soft delete flag - true if record is deleted';
COMMENT ON COLUMN accounts.deleted_at IS 'Timestamp when record was soft deleted';