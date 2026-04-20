-- V1.29 Revert order_lines and pricelist_versions auditable columns to UUID
-- These specific entities do not extend AuditableEntity and explicitly use UUID in Java.

DO $$
BEGIN
    ALTER TABLE order_lines ALTER COLUMN created_by TYPE UUID USING created_by::uuid;
    ALTER TABLE order_lines ALTER COLUMN updated_by TYPE UUID USING updated_by::uuid;
    
    ALTER TABLE pricelist_versions ALTER COLUMN created_by TYPE UUID USING created_by::uuid;
    ALTER TABLE pricelist_versions ALTER COLUMN updated_by TYPE UUID USING updated_by::uuid;
END $$;
