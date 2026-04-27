-- V1.50 Add is_credit flag to invoices
ALTER TABLE invoices ADD COLUMN is_credit BOOLEAN DEFAULT FALSE;
