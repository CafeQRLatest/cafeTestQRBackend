-- V1.28 Fix IsActive Column Types
-- Hibernate strictly expects VARCHAR for String mappings, but V1.23 used CHAR(1).

DO $$
DECLARE
    t_name text;
BEGIN
    FOR t_name IN 
        SELECT unnest(ARRAY[
            'currencies',
            'pricelists',
            'pricelist_versions',
            'customers',
            'vendors',
            'product_prices',
            'product_purchase_prices',
            'orders',
            'order_lines',
            'invoices',
            'payments'
        ])
    LOOP
        EXECUTE format('ALTER TABLE %I ALTER COLUMN isactive TYPE VARCHAR(1)', t_name);
    END LOOP;
END $$;
