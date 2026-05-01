-- =============================================
-- V1_54: Expense Production Upgrade
-- =============================================

-- Add updated_by to base entity tables globally (can be useful for others, but targeting expenses specifically)

-- Add missing columns to expense_categories
ALTER TABLE expense_categories ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);

-- Add missing columns to expenses
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS reference_number VARCHAR(20);

-- Make reference_number unique per tenant
CREATE UNIQUE INDEX IF NOT EXISTS idx_expenses_ref_num ON expenses(client_id, reference_number);

-- Update existing expenses with a dummy reference number to satisfy any future NOT NULL constraints or just to have data
-- Create sequence for atomic reference number generation
CREATE SEQUENCE IF NOT EXISTS expense_ref_seq START 1;

WITH numbered_expenses AS (
  SELECT id, ROW_NUMBER() OVER (ORDER BY created_at ASC) as row_num
  FROM expenses
  WHERE reference_number IS NULL
)
UPDATE expenses e
SET reference_number = 'EXP-' || to_char(EXTRACT(YEAR FROM e.created_at), 'FM9999') || '-' || to_char(n.row_num, 'FM00000')
FROM numbered_expenses n
WHERE e.id = n.id;

-- Add idempotency_key for safe duplicate prevention
ALTER TABLE expenses ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(100);
CREATE UNIQUE INDEX IF NOT EXISTS idx_expenses_idempotency ON expenses(client_id, org_id, idempotency_key);

-- Make expense category names unique per branch
CREATE UNIQUE INDEX IF NOT EXISTS idx_expense_categories_name ON expense_categories(client_id, org_id, name);
