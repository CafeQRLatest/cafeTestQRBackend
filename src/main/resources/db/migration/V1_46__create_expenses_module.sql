-- =============================================
-- V1_46: Expenses Module
-- =============================================

-- Expense Categories
CREATE TABLE IF NOT EXISTS expense_categories (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    sort_order  INT DEFAULT 0,
    is_active   VARCHAR(1) DEFAULT 'Y',
    client_id   UUID,
    org_id      UUID,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(255)
);

-- Expenses
CREATE TABLE IF NOT EXISTS expenses (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id     UUID REFERENCES expense_categories(id),
    expense_date    TIMESTAMP NOT NULL DEFAULT NOW(),
    amount          NUMERIC(15,2) NOT NULL DEFAULT 0,
    description     TEXT,
    payment_method  VARCHAR(30),
    is_active       VARCHAR(1) DEFAULT 'Y',
    client_id       UUID,
    org_id          UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_expenses_client     ON expenses(client_id);
CREATE INDEX IF NOT EXISTS idx_expenses_org        ON expenses(org_id);
CREATE INDEX IF NOT EXISTS idx_expenses_date       ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_category   ON expenses(category_id);
CREATE INDEX IF NOT EXISTS idx_exp_cat_client       ON expense_categories(client_id);

-- Add Expenses menu item
INSERT INTO menus (id, name, url, description, parent_id, isactive, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Expenses',
  '/owner/expenses',
  'Expense Tracking & Profit',
  NULL,
  'Y',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Assign Expenses menu to all roles
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.url = '/owner/expenses'
  AND r.name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')
ON CONFLICT DO NOTHING;
