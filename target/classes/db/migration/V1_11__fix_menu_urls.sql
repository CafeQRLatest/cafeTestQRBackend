-- V1.11 Rename Menu to Product Management and Map to Global Roles

-- 0. Ensure unique constraint exists for global roles to support ON CONFLICT
CREATE UNIQUE INDEX IF NOT EXISTS idx_roles_name_global ON roles (name) WHERE client_id IS NULL;

-- 0.1 Fix Missing Auditing Columns in various tables (Hibernate Schema Validation Sync)
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE roles ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE terminals ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE clients ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE devices ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE products ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE categories ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE roles ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE terminals ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE organizations ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE clients ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE devices ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE orders ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE invoices ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE products ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE categories ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

DO $$ 
DECLARE
    super_admin_role_id UUID;
    admin_role_id UUID;
    manager_role_id UUID;
    prod_mgmt_id UUID;
    pos_id UUID;
    dash_id UUID;
BEGIN 
    -- 1. Rename Menu and Fix URL
    UPDATE menus 
    SET name = 'Product Management', 
        url = '/owner/product-management',
        description = 'Prices & Inventory'
    WHERE name = 'Menu Management' OR name = 'Product Management';

    -- Fix Organization URL (singular vs plural in V1.9)
    UPDATE menus SET url = '/admin/organization' WHERE name = 'Organization';
    
    -- 2. Get Menu IDs
    SELECT id INTO prod_mgmt_id FROM menus WHERE name = 'Product Management';
    SELECT id INTO pos_id FROM menus WHERE name = 'Point of Sale';
    SELECT id INTO dash_id FROM menus WHERE name = 'Dashboard';

    -- 3. Ensure Global Roles Exist (client_id IS NULL)
    INSERT INTO roles (id, name, description, client_id, isactive)
    VALUES 
        (gen_random_uuid(), 'SUPER_ADMIN', 'Global System Administrator', NULL, 'Y'),
        (gen_random_uuid(), 'ADMIN', 'Global Business Administrator', NULL, 'Y'),
        (gen_random_uuid(), 'MANAGER', 'Global Branch Manager', NULL, 'Y')
    ON CONFLICT (name) WHERE client_id IS NULL DO NOTHING;

    -- 4. Get Role IDs for Global Roles
    SELECT id INTO super_admin_role_id FROM roles WHERE name = 'SUPER_ADMIN' AND client_id IS NULL;
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN' AND client_id IS NULL;
    SELECT id INTO manager_role_id FROM roles WHERE name = 'MANAGER' AND client_id IS NULL;

    -- 5. Link Menus to Roles (Access in role_menus)
    -- SUPER_ADMIN gets everything
    INSERT INTO role_menus (role_id, menu_id)
    SELECT super_admin_role_id, id FROM menus
    ON CONFLICT DO NOTHING;

    -- ADMIN and MANAGER get POS, Product Management, and Dashboard
    IF admin_role_id IS NOT NULL THEN
        INSERT INTO role_menus (role_id, menu_id) VALUES (admin_role_id, prod_mgmt_id) ON CONFLICT DO NOTHING;
        INSERT INTO role_menus (role_id, menu_id) VALUES (admin_role_id, pos_id) ON CONFLICT DO NOTHING;
        INSERT INTO role_menus (role_id, menu_id) VALUES (admin_role_id, dash_id) ON CONFLICT DO NOTHING;
    END IF;

    IF manager_role_id IS NOT NULL THEN
        INSERT INTO role_menus (role_id, menu_id) VALUES (manager_role_id, prod_mgmt_id) ON CONFLICT DO NOTHING;
        INSERT INTO role_menus (role_id, menu_id) VALUES (manager_role_id, pos_id) ON CONFLICT DO NOTHING;
        INSERT INTO role_menus (role_id, menu_id) VALUES (manager_role_id, dash_id) ON CONFLICT DO NOTHING;
    END IF;

END $$;
