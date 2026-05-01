-- =============================================
-- V1_59: Add type columns to invoices and payments
-- These support the unified financial model where SALE/PURCHASE/EXPENSE
-- orders all share the same invoices and payments tables, distinguished
-- by type for reporting and accounting.
-- =============================================

-- invoices: classify the document type
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS invoice_type VARCHAR(30);

-- payments: classify the direction of cash flow
ALTER TABLE payments ADD COLUMN IF NOT EXISTS payment_type VARCHAR(20);

-- Back-fill existing SALE-originated invoices and payments
-- (all pre-existing records are from SALE orders)
UPDATE invoices SET invoice_type = 'CUSTOMER_INVOICE' WHERE invoice_type IS NULL;
UPDATE payments SET payment_type = 'INBOUND'          WHERE payment_type IS NULL;

-- expenses table was dropped in V1_58; expense_category_id already added in V1_57
-- order_type column already existed as VARCHAR, Hibernate @Enumerated(STRING) needs no migration
