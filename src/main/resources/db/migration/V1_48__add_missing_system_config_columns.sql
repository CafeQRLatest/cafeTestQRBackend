-- V1_48: Add all missing columns to system_configurations table
-- The entity has tax-related fields that were never created by any migration.
-- Also ensures the discount_enabled and pos_product_listing_enabled columns exist
-- (V1_44 and V1_45 may have failed if the table didn't exist yet).

-- Tax-related columns (added to entity but never migrated)
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS tax_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS prices_include_tax BOOLEAN DEFAULT FALSE;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS tax_rates_json TEXT;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS tax_default_id VARCHAR(255);
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS tax_split_enabled BOOLEAN DEFAULT FALSE;

-- Feature toggle columns (may be missing from earlier failed migrations)
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS pos_product_listing_enabled BOOLEAN DEFAULT TRUE;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS discount_enabled BOOLEAN DEFAULT TRUE;

-- Receipt/Print columns from V1_22 (may have failed due to DO $$ block)
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS bill_footer TEXT;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_logo_bitmap TEXT;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_logo_cols INTEGER;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_logo_rows INTEGER;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS paper_mm VARCHAR(10);
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_cols INTEGER;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_left_margin_dots INTEGER;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_right_margin_dots INTEGER;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_auto_cut BOOLEAN DEFAULT FALSE;
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_win_list_url VARCHAR(512);
ALTER TABLE system_configurations ADD COLUMN IF NOT EXISTS print_win_post_url VARCHAR(512);
