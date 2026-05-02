-- =============================================
-- V1_64: Performance Hardening for Orders & Expenses
-- Adds missing performance indices for large scale transaction filtering
-- =============================================

-- 1. Core Transaction Indices (orders table)
-- Optimized for: Filtering by Type (SALE/EXPENSE), Date Ranges, and Categorization
CREATE INDEX IF NOT EXISTS idx_orders_type ON orders(order_type);
CREATE INDEX IF NOT EXISTS idx_orders_date ON orders(order_date);
CREATE INDEX IF NOT EXISTS idx_orders_expense_cat ON orders(expense_category_id);
CREATE INDEX IF NOT EXISTS idx_orders_active ON orders(isactive);

-- 2. Expense Category Master Indices
-- Optimized for: Multi-branch organizational attribution and status filtering
CREATE INDEX IF NOT EXISTS idx_exp_cat_org ON expense_categories(org_id);
CREATE INDEX IF NOT EXISTS idx_exp_cat_active ON expense_categories(is_active);
CREATE INDEX IF NOT EXISTS idx_exp_cat_sort ON expense_categories(sort_order);

-- 3. Composite Indices for Common Query Patterns
-- Client + Org + Date (Very common for dashboard reporting)
CREATE INDEX IF NOT EXISTS idx_orders_tenant_date ON orders(client_id, org_id, order_date DESC);
