-- V1.26 Enhance Currency & Pricelist with additional ERP fields

DO $$
BEGIN
    -- Currency: add description, decimal_places, country_code
    ALTER TABLE currencies ADD COLUMN IF NOT EXISTS description TEXT;
    ALTER TABLE currencies ADD COLUMN IF NOT EXISTS decimal_places INTEGER DEFAULT 2;
    ALTER TABLE currencies ADD COLUMN IF NOT EXISTS country_code VARCHAR(10);

    -- Pricelist: add description, discount_percentage, markup_percentage
    ALTER TABLE pricelists ADD COLUMN IF NOT EXISTS description TEXT;
    ALTER TABLE pricelists ADD COLUMN IF NOT EXISTS discount_percentage DECIMAL(5, 2) DEFAULT 0;
    ALTER TABLE pricelists ADD COLUMN IF NOT EXISTS markup_percentage DECIMAL(5, 2) DEFAULT 0;

    RAISE NOTICE 'Enhanced currencies and pricelists with additional fields.';
END $$;
