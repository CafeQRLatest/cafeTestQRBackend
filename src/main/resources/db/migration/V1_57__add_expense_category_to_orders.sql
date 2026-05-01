-- =============================================
-- V1_57: Add expense_category_id to core accounting tables
-- =============================================

ALTER TABLE orders ADD COLUMN IF NOT EXISTS expense_category_id UUID REFERENCES expense_categories(id);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS expense_category_id UUID REFERENCES expense_categories(id);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS expense_category_id UUID REFERENCES expense_categories(id);
