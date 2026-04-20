-- V1.24 Add ERP Module Menus (Partners & Organization sub-menus)
-- Creates: "Partners" menu (Customers & Vendors) and adds Currency/PriceList to Organization

DO $$
DECLARE
    partners_menu_id UUID;
    super_admin_role_id UUID;
    admin_role_id UUID;
BEGIN
    ---------------------------------------------------------------------------
    -- 1. Insert the "Partners" top-level menu
    ---------------------------------------------------------------------------
    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Partners', '/owner/partners', 'Customers & Vendor Management', 'Y')
    ON CONFLICT DO NOTHING;

    SELECT id INTO partners_menu_id FROM menus WHERE name = 'Partners';

    ---------------------------------------------------------------------------
    -- 2. Grant access to SUPER_ADMIN and ADMIN
    ---------------------------------------------------------------------------
    SELECT id INTO super_admin_role_id FROM roles WHERE name = 'SUPER_ADMIN' AND client_id IS NULL;
    SELECT id INTO admin_role_id FROM roles WHERE name = 'ADMIN' AND client_id IS NULL;

    IF super_admin_role_id IS NOT NULL AND partners_menu_id IS NOT NULL THEN
        INSERT INTO role_menus (role_id, menu_id)
        VALUES (super_admin_role_id, partners_menu_id)
        ON CONFLICT DO NOTHING;
    END IF;

    IF admin_role_id IS NOT NULL AND partners_menu_id IS NOT NULL THEN
        INSERT INTO role_menus (role_id, menu_id)
        VALUES (admin_role_id, partners_menu_id)
        ON CONFLICT DO NOTHING;
    END IF;

    ---------------------------------------------------------------------------
    -- 3. Insert "Purchase Orders" menu
    ---------------------------------------------------------------------------
    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Purchase Orders', '/owner/purchase-orders', 'Purchase Order Management', 'Y')
    ON CONFLICT DO NOTHING;

    -- Grant to SUPER_ADMIN and ADMIN
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id
    FROM roles r CROSS JOIN menus m
    WHERE r.name IN ('SUPER_ADMIN', 'ADMIN') AND r.client_id IS NULL AND m.name = 'Purchase Orders'
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'ERP menus created: Partners (%), Purchase Orders', partners_menu_id;
END $$;
