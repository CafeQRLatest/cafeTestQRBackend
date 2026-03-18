-- V1.17 Add org_id to remaining master tables for multi-org support
DO $$ 
BEGIN 
    -- 1. UOMs
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='uoms' AND column_name='org_id') THEN
        ALTER TABLE uoms ADD COLUMN org_id UUID;
    END IF;

    -- 2. Variant Groups
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='variant_groups' AND column_name='org_id') THEN
        ALTER TABLE variant_groups ADD COLUMN org_id UUID;
    END IF;

    -- 3. Variant Options
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='variant_options' AND column_name='org_id') THEN
        ALTER TABLE variant_options ADD COLUMN org_id UUID;
    END IF;

    -- 4. Product Variant Mappings
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='product_variant_mappings' AND column_name='org_id') THEN
        ALTER TABLE product_variant_mappings ADD COLUMN org_id UUID;
    END IF;

    -- 5. Variant Pricing (Overrides)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='variant_pricing' AND column_name='org_id') THEN
        ALTER TABLE variant_pricing ADD COLUMN org_id UUID;
    END IF;

    -- 6. Product Upsells (Add-ons)
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='product_upsells' AND column_name='org_id') THEN
        ALTER TABLE product_upsells ADD COLUMN org_id UUID;
    END IF;

END $$;
