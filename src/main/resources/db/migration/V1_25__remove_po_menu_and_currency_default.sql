-- V1.25 Add is_default to currencies table

DO $$
BEGIN
    -- Add is_default column to currencies
    ALTER TABLE currencies ADD COLUMN IF NOT EXISTS is_default BOOLEAN DEFAULT false;
    RAISE NOTICE 'Added is_default to currencies.';
END $$;
