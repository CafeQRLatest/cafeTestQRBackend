-- Add missing updated_at column to audit_logs table
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
