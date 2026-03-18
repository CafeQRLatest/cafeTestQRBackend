-- V1.14 Fix missing auditing columns in organizations table
-- Standardizing the schema for AuditableEntity and ensuring core fields exist

DO $$ 
BEGIN
    -- 1. Add Auditing Columns
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='created_at') THEN
        ALTER TABLE organizations ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='updated_at') THEN
        ALTER TABLE organizations ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='created_by') THEN
        ALTER TABLE organizations ADD COLUMN created_by VARCHAR(255);
    END IF;

    -- 2. Ensure Core Contact Fields
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='name') THEN
        ALTER TABLE organizations ADD COLUMN name VARCHAR(255);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='address') THEN
        ALTER TABLE organizations ADD COLUMN address TEXT;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='phone') THEN
        ALTER TABLE organizations ADD COLUMN phone VARCHAR(50);
    END IF;

END $$;
