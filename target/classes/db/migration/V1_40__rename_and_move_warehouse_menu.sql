-- V1_40__rename_and_move_warehouse_menu.sql
DO $$
DECLARE
    org_menu_id UUID;
    warehouse_menu_id UUID;
BEGIN
    -- 1. Find Organization Menu
    SELECT id INTO org_menu_id FROM menus WHERE name = 'Organization' LIMIT 1;
    
    -- 2. Find and Rename 'Warehouses' menu to 'Warehouse Management'
    SELECT id INTO warehouse_menu_id FROM menus WHERE name IN ('Warehouses', 'Warehouse Management') LIMIT 1;
    
    IF warehouse_menu_id IS NOT NULL THEN
        UPDATE menus 
        SET name = 'Warehouse Management',
            parent_id = org_menu_id
        WHERE id = warehouse_menu_id;
    END IF;

    -- 3. Ensure universal access (Grant to SUPER_ADMIN and ADMIN role)
    IF warehouse_menu_id IS NOT NULL THEN
        INSERT INTO role_menus (role_id, menu_id)
        SELECT r.id, warehouse_menu_id
        FROM roles r
        WHERE r.name IN ('SUPER_ADMIN', 'ADMIN') AND r.client_id IS NULL
        ON CONFLICT DO NOTHING;
    END IF;

END $$;
