-- V1.38 Add Stock Sub-Modules: Valuation, Transfer Reports, Adjustment Reports
-- Updates the Stock parent URL and inserts child menus

DO $$
DECLARE
    stock_menu_id UUID;
BEGIN
    -- 1. Get the Stock parent menu ID
    SELECT id INTO stock_menu_id FROM menus WHERE name = 'Stock' AND parent_id IS NULL LIMIT 1;

    IF stock_menu_id IS NULL THEN
        RAISE NOTICE 'Stock parent menu not found, skipping sub-module creation';
        RETURN;
    END IF;

    -- 2. Update Stock parent URL to point to the hub page
    UPDATE menus SET url = '/owner/stock-menu' WHERE id = stock_menu_id;

    -- 3. Insert Stock Valuation sub-menu
    IF NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Stock Valuation') THEN
        INSERT INTO menus (id, name, url, description, isactive, parent_id)
        VALUES (gen_random_uuid(), 'Stock Valuation', '/owner/stock-valuation', 'Inventory value by warehouse', 'Y', stock_menu_id);
    END IF;

    -- 4. Insert Stock Transfer Reports sub-menu
    IF NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Transfer Reports') THEN
        INSERT INTO menus (id, name, url, description, isactive, parent_id)
        VALUES (gen_random_uuid(), 'Transfer Reports', '/owner/stock-transfer-reports', 'Transfer movement reports', 'Y', stock_menu_id);
    END IF;

    -- 5. Insert Stock Adjustment Reports sub-menu
    IF NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Adjustment Reports') THEN
        INSERT INTO menus (id, name, url, description, isactive, parent_id)
        VALUES (gen_random_uuid(), 'Adjustment Reports', '/owner/stock-adjustment-reports', 'Adjustment audit reports', 'Y', stock_menu_id);
    END IF;

    -- 6. Grant access to SUPER_ADMIN and ADMIN for the new menus
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id
    FROM roles r CROSS JOIN menus m
    WHERE r.name IN ('SUPER_ADMIN', 'ADMIN')
      AND r.client_id IS NULL
      AND m.name IN ('Stock Valuation', 'Transfer Reports', 'Adjustment Reports')
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'V1.38 Stock sub-modules added successfully!';
END $$;
