-- V1.16 Add Product Links and Upsells
DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Product Variant Mappings Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS product_variant_mappings (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
        variant_group_id UUID NOT NULL REFERENCES variant_groups(id) ON DELETE CASCADE,
        is_required BOOLEAN NOT NULL DEFAULT TRUE,
        client_id UUID,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255)
    );

    ---------------------------------------------------------------------------
    -- 2. Variant Pricing (Overrides) Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS variant_pricing (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
        variant_option_id UUID NOT NULL REFERENCES variant_options(id) ON DELETE CASCADE,
        override_price NUMERIC(19, 2), -- If null, use base additional_price from variant_options
        is_available BOOLEAN NOT NULL DEFAULT TRUE,
        client_id UUID,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255)
    );

    ---------------------------------------------------------------------------
    -- 3. Product Upsells (Add-ons) Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS product_upsells (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        parent_product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
        upsell_product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
        is_active BOOLEAN NOT NULL DEFAULT TRUE,
        client_id UUID,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by VARCHAR(255),
        UNIQUE(parent_product_id, upsell_product_id)
    );

END $$;
