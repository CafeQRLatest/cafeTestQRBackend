-- V1.12 Refine Product Schema and Add UOM Management
DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. UOM Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS uoms (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(100) NOT NULL,
        short_name VARCHAR(20) NOT NULL,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        is_default BOOLEAN NOT NULL DEFAULT FALSE,
        uom_precision INTEGER NOT NULL DEFAULT 0,
        client_id UUID, -- Shared across enterprise
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255)
    );

    ---------------------------------------------------------------------------
    -- 2. Refine Products Table
    ---------------------------------------------------------------------------
    -- Add New ERP Columns
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='product_type') THEN
        ALTER TABLE products ADD COLUMN product_type VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='is_variant') THEN
        ALTER TABLE products ADD COLUMN is_variant BOOLEAN DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='is_packaged_good') THEN
        ALTER TABLE products ADD COLUMN is_packaged_good BOOLEAN DEFAULT FALSE;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='product_code') THEN
        ALTER TABLE products ADD COLUMN product_code VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='uom_id') THEN
        ALTER TABLE products ADD COLUMN uom_id UUID REFERENCES uoms(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='is_active') THEN
        ALTER TABLE products ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
    END IF;

    -- Add Global ERP Financials & Inventory
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='tax_rate') THEN
        ALTER TABLE products ADD COLUMN tax_rate NUMERIC(19, 2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='tax_code') THEN
        ALTER TABLE products ADD COLUMN tax_code VARCHAR(100);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='mrp') THEN
        ALTER TABLE products ADD COLUMN mrp NUMERIC(19, 2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='cost_price') THEN
        ALTER TABLE products ADD COLUMN cost_price NUMERIC(19, 2);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='barcode') THEN
        ALTER TABLE products ADD COLUMN barcode VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='min_stock_level') THEN
        ALTER TABLE products ADD COLUMN min_stock_level INTEGER;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='kds_station') THEN
        ALTER TABLE products ADD COLUMN kds_station VARCHAR(255);
    END IF;

    -- Ensure Products has client_id as well
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='client_id') THEN
        ALTER TABLE products ADD COLUMN client_id UUID;
    END IF;

    -- Remove Legacy/Unnecessary Columns
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='dietary_preference') THEN
        ALTER TABLE products DROP COLUMN dietary_preference;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='uom') THEN
        ALTER TABLE products DROP COLUMN uom;
    END IF;

END $$;
