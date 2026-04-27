-- V1_47: Safety net - ensure all columns expected by JPA entities exist on the users table
-- This fixes issues where V1_1's DO $$ block may not have fully executed
-- on managed PostgreSQL (Supabase pooler mode).

-- Columns added by V1_1 that might be missing:
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS isactive CHAR(1) DEFAULT 'Y';
ALTER TABLE users ADD COLUMN IF NOT EXISTS role_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS terminal_id UUID;
ALTER TABLE users ADD COLUMN IF NOT EXISTS org_id UUID;

-- Ensure the roles table exists (needed for the foreign key)
CREATE TABLE IF NOT EXISTS roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    isactive CHAR(1) DEFAULT 'Y',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255)
);

-- Ensure the terminals table exists
CREATE TABLE IF NOT EXISTS terminals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID,
    org_id UUID,
    name VARCHAR(100) NOT NULL,
    terminal_code VARCHAR(50) UNIQUE,
    device_type VARCHAR(50),
    ip_address VARCHAR(50),
    isactive CHAR(1) DEFAULT 'Y',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    tenant_id VARCHAR(255)
);

-- Ensure permissions table exists
CREATE TABLE IF NOT EXISTS permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ensure role_permissions mapping table exists
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Seed Default Permissions (if not already present)
INSERT INTO permissions (name, description) VALUES
('CREATE_ORDER', 'Allow creating new orders'),
('VOID_BILL', 'Allow voiding paid bills'),
('VIEW_REPORT', 'Allow viewing sales reports'),
('MANAGE_USERS', 'Allow managing staff accounts'),
('MANAGE_ORG', 'Allow managing branch details'),
('MANAGE_TERMINAL', 'Allow managing POS devices')
ON CONFLICT (name) DO NOTHING;
