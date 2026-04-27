-- V1.51 Add Audit Trail fields for Order Revisions
ALTER TABLE orders ADD COLUMN original_order_id UUID;
ALTER TABLE orders ADD COLUMN revision_number INTEGER DEFAULT 0;

ALTER TABLE invoices ADD COLUMN original_invoice_id UUID;

-- Add indices for faster audit lookups
CREATE INDEX idx_orders_original ON orders(original_order_id);
CREATE INDEX idx_invoices_original ON invoices(original_invoice_id);
