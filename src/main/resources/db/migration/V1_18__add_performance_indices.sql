-- V1.18 Add Performance Indices for Multi-Tenancy
DO $$ 
BEGIN 
    -- 1. Products (client_id, org_id)
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_products_client_org') THEN
        CREATE INDEX idx_products_client_org ON products (client_id, org_id);
    END IF;

    -- 2. Categories (client_id, org_id)
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_categories_client_org') THEN
        CREATE INDEX idx_categories_client_org ON categories (client_id, org_id);
    END IF;

    -- 3. UOMs (client_id, org_id)
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_uoms_client_org') THEN
        CREATE INDEX idx_uoms_client_org ON uoms (client_id, org_id);
    END IF;

    -- 4. Variant Groups (client_id, org_id)
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_variant_groups_client_org') THEN
        CREATE INDEX idx_variant_groups_client_org ON variant_groups (client_id, org_id);
    END IF;

    -- 5. Orders (client_id, org_id)
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_orders_client_org') THEN
        CREATE INDEX idx_orders_client_org ON orders (client_id, org_id);
    END IF;

    -- 6. Users (client_id, org_id)
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_users_client_org') THEN
        CREATE INDEX idx_users_client_org ON users (client_id, org_id);
    END IF;

    -- 7. Add columns for upsells and mappings indices
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_upsells_client_org') THEN
        CREATE INDEX idx_upsells_client_org ON product_upsells (client_id, org_id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_variant_mappings_client_org') THEN
        CREATE INDEX idx_variant_mappings_client_org ON product_variant_mappings (client_id, org_id);
    END IF;

END $$;
