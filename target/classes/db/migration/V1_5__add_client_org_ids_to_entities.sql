-- Add missing client_id and org_id to all tables inheriting from BaseEntity
-- Use IF NOT EXISTS to prevent errors if the column already exists from a previous migration

ALTER TABLE categories ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE products ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE products ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE orders ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE order_items ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE invoices ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE terminals ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE terminals ADD COLUMN IF NOT EXISTS org_id UUID;

ALTER TABLE users ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS org_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS terminal_id UUID;

ALTER TABLE roles ADD COLUMN IF NOT EXISTS client_id UUID;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS org_id UUID;

-- Organizations needs client_id, but logically doesn't need org_id since its Id serves that purpose
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS client_id UUID;
