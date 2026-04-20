-- V1.27 Fix Auditable Columns Type
-- AuditableEntity in Java uses String (VARCHAR), but V1.23 created these as UUID.
-- Altering them to VARCHAR(255) to pass Hibernate schema validation.

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
            'invoice_lines',
            'payments'
        ])
    LOOP
        EXECUTE format('ALTER TABLE %I ALTER COLUMN created_by TYPE VARCHAR(255)', t_name);
        EXECUTE format('ALTER TABLE %I ALTER COLUMN updated_by TYPE VARCHAR(255)', t_name);
    END LOOP;
END $$;
