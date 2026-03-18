-- V1.7 Enterprise Auth & Session Management
-- Support for Refresh Token Rotation, Revocation, and Audit Log enhancements.

DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Refresh Tokens Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS refresh_tokens (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        token VARCHAR(512) UNIQUE NOT NULL,
        user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
        client_id UUID REFERENCES clients(id) ON DELETE CASCADE,
        expiry_date TIMESTAMP NOT NULL,
        revoked_at TIMESTAMP,
        replaced_by_token VARCHAR(512),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by_ip VARCHAR(50),
        user_agent TEXT
    );

    CREATE INDEX IF NOT EXISTS idx_refresh_token_value ON refresh_tokens(token);
    CREATE INDEX IF NOT EXISTS idx_refresh_token_user ON refresh_tokens(user_id);

    ---------------------------------------------------------------------------
    -- 2. Audit Log Enhancements
    ---------------------------------------------------------------------------
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_logs' AND column_name='user_agent') THEN
        ALTER TABLE audit_logs ADD COLUMN user_agent TEXT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_logs' AND column_name='request_url') THEN
        ALTER TABLE audit_logs ADD COLUMN request_url VARCHAR(512);
    END IF;

END $$;
