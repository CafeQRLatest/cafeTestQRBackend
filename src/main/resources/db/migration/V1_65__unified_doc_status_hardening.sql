-- =============================================
-- V1_65: Unified Document Status Hardening
-- Adds doc_status to Order, Invoice, and Payment for standardized auditing
-- =============================================

-- 1. Orders table (Base for Sales, Purchases, and Expenses)
ALTER TABLE orders ADD COLUMN IF NOT EXISTS doc_status VARCHAR(20) DEFAULT 'COMPLETED';

-- 2. Invoices table
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS doc_status VARCHAR(20) DEFAULT 'COMPLETED';

-- 3. Payments table
ALTER TABLE payments ADD COLUMN IF NOT EXISTS doc_status VARCHAR(20) DEFAULT 'COMPLETED';

-- Initial Data Sync: Populate doc_status from existing status columns where applicable
UPDATE orders SET doc_status = order_status WHERE order_status IS NOT NULL;
UPDATE invoices SET doc_status = status WHERE status IS NOT NULL;
UPDATE payments SET doc_status = status WHERE status IS NOT NULL;
