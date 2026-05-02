-- =============================================
-- V1_66: Audit Suffix Capacity Hardening
-- Increases length of document number columns to accommodate VOID suffixes
-- =============================================

-- 1. Orders table
ALTER TABLE orders ALTER COLUMN order_no TYPE VARCHAR(100);

-- 2. Invoices table
ALTER TABLE invoices ALTER COLUMN invoice_no TYPE VARCHAR(100);

-- 3. Payments table
ALTER TABLE payments ALTER COLUMN reference_no TYPE VARCHAR(100);
