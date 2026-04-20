-- V1.20 Add Configurations Menu (Super Admin Only)
-- Creates the "Configurations" screen menu entry and grants access exclusively to SUPER_ADMIN.

DO $$
DECLARE
    config_menu_id UUID;
    super_admin_role_id UUID;
BEGIN
    ---------------------------------------------------------------------------
    -- 1. Insert the Configurations menu entry (idempotent)
    ---------------------------------------------------------------------------
    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Configurations', '/owner/configurations', 'POS Configuration Engine — Power Modules, Tax, Round-off, Print & Hardware', 'Y')
    ON CONFLICT DO NOTHING;

    -- Grab its ID (handles both fresh insert and already-existing row)
    SELECT id INTO config_menu_id FROM menus WHERE name = 'Configurations';

    ---------------------------------------------------------------------------
    -- 2. Grant access to SUPER_ADMIN only
    ---------------------------------------------------------------------------
    SELECT id INTO super_admin_role_id FROM roles WHERE name = 'SUPER_ADMIN' AND client_id IS NULL;

    IF super_admin_role_id IS NOT NULL AND config_menu_id IS NOT NULL THEN
        INSERT INTO role_menus (role_id, menu_id)
        VALUES (super_admin_role_id, config_menu_id)
        ON CONFLICT DO NOTHING;
    END IF;

    -- NOTE: ADMIN, MANAGER, STAFF are intentionally NOT granted access.
    -- This screen is restricted to SUPER_ADMIN only.

    RAISE NOTICE 'Configurations menu created (%) and mapped to SUPER_ADMIN (%)', config_menu_id, super_admin_role_id;
END $$;
