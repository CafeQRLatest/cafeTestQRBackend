-- V1.13 Add Variants Schema
DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Variant Groups Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS variant_groups (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(100) NOT NULL,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        client_id UUID, -- Partitioned by client
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255)
    );

    ---------------------------------------------------------------------------
    -- 2. Variant Options Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS variant_options (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        group_id UUID NOT NULL REFERENCES variant_groups(id) ON DELETE CASCADE,
        name VARCHAR(100) NOT NULL,
        additional_price NUMERIC(19, 2) DEFAULT 0,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        client_id UUID, -- Also at client level
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255)
    );

END $$;
