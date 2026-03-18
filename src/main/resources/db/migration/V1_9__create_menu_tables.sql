-- V1.9 Create Menu Tables and Seed Data
-- Migration to support role-based dynamic dashboard menus.

DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Menus Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS menus (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(255) NOT NULL,
        url VARCHAR(255) NOT NULL,
        description TEXT,
        parent_id UUID REFERENCES menus(id),
        isactive VARCHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    -- Ensure parent_id column exists if table was already created
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='menus' AND column_name='parent_id') THEN
        ALTER TABLE menus ADD COLUMN parent_id UUID REFERENCES menus(id);
    END IF;

    ---------------------------------------------------------------------------
    -- 2. Role-Menus Mapping Table
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS role_menus (
        role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
        menu_id UUID REFERENCES menus(id) ON DELETE CASCADE,
        PRIMARY KEY (role_id, menu_id)
    );

    ---------------------------------------------------------------------------
    -- 3. Seed Initial Menus
    ---------------------------------------------------------------------------
    -- Parent Menus
    INSERT INTO menus (name, url, description) VALUES 
    ('Dashboard', '/dashboard', 'Business analytics and live overview.'),
    ('Point of Sale', '/counter', 'New Counter Orders'),
    ('Menu Management', '/menu', 'Prices & Availability'),
    ('Reports & Billing', '/billing', 'Invoices & Tax Data'),
    ('Organization', '/admin/organization', 'Branch & Hardware Management'),
    ('Subscription', '/subscription', 'Billing & Plans')
    ON CONFLICT DO NOTHING;

    -- Sub-Menus (Linked to Organization)
    -- INSERT INTO menus (name, url, description, parent_id)
    -- SELECT sub.name, sub.url, sub.description, p.id
    -- FROM (VALUES 
    --     ('Client Management', '/admin/client-profile', 'Enterprise Details'),
    --     ('Branch Management', '/admin/organization-details', 'Branches & Locations'),
    --     ('Device Management', '/admin/devices', 'Hardware Inventory'),
    --     ('Staff & Permissions', '/admin/users', 'Role Access Control')
    -- ) as sub(name, url, description)
    -- CROSS JOIN menus p 
    -- WHERE p.name = 'Organization'
    -- ON CONFLICT DO NOTHING;

    ---------------------------------------------------------------------------
    -- 4. Default Role Mapping
    ---------------------------------------------------------------------------
    -- Grant ALL menus to every SUPER_ADMIN
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id 
    FROM roles r
    CROSS JOIN menus m
    WHERE r.name = 'SUPER_ADMIN'
    ON CONFLICT DO NOTHING;

    -- Grant specific menus to ADMIN as requested (Menu Management & POS)
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id 
    FROM roles r
    CROSS JOIN menus m
    WHERE r.name = 'ADMIN' 
    AND m.name IN ('Point of Sale', 'Menu Management', 'Dashboard')
    ON CONFLICT DO NOTHING;

    -- Grant POS menu to every STAFF
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id 
    FROM roles r
    CROSS JOIN menus m
    WHERE r.name = 'STAFF' 
    AND m.name IN ('Point of Sale')
    ON CONFLICT DO NOTHING;

END $$;
