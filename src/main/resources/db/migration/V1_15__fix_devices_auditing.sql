-- V1.15 Fix missing auditing columns in devices table
-- Standardizing the schema for AuditableEntity and ensuring consistent defaults

DO $$ 
BEGIN
    -- 1. Add missing created_by column
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='devices' AND column_name='created_by') THEN
        ALTER TABLE devices ADD COLUMN created_by VARCHAR(255);
    END IF;

    -- 2. Ensure created_at and updated_at have consistent defaults if they don't already
    -- Note: V1_8 already created them, but we want to be safe during saving
    ALTER TABLE devices ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
    ALTER TABLE devices ALTER COLUMN updated_at SET DEFAULT CURRENT_TIMESTAMP;

END $$;
