-- V1.34 Add warehouse_id to orders table (Repair missing column)
-- This column was originally in V1.27, but may have been skipped due to version conflicts.

DO $$
BEGIN
    -- Add warehouse_id to orders
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='orders' AND column_name='warehouse_id') THEN
        ALTER TABLE orders ADD COLUMN warehouse_id UUID REFERENCES warehouses(id);
        RAISE NOTICE 'Added warehouse_id to orders table.';
    END IF;
END $$;
