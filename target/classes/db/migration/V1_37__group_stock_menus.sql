DO $$
DECLARE
    stock_menu_id UUID;
BEGIN
    -- 1. Create a parent 'Stock' menu if it does not exist
    IF NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Stock' AND parent_id IS NULL) THEN
        stock_menu_id := gen_random_uuid();
        INSERT INTO menus (id, name, url, description, isactive)
        VALUES (stock_menu_id, 'Stock', '/owner/stock-overview', 'Manage inventory and stock flows', 'Y');
    ELSE
        SELECT id INTO stock_menu_id FROM menus WHERE name = 'Stock' AND parent_id IS NULL LIMIT 1;
    END IF;

    -- 2. Create 'Stock History' if it doesn't exist
    IF NOT EXISTS (SELECT 1 FROM menus WHERE name = 'Stock History') THEN
        INSERT INTO menus (id, name, url, description, isactive, parent_id)
        VALUES (gen_random_uuid(), 'Stock History', '/owner/stock-history', 'View stock ledgers', 'Y', stock_menu_id);
    ELSE
        UPDATE menus SET parent_id = stock_menu_id WHERE name = 'Stock History';
    END IF;

    -- 3. Move other stock related menus under 'Stock'
    UPDATE menus SET parent_id = stock_menu_id WHERE name IN ('Stock Overview', 'Stock Transfers', 'Stock Adjustments');
END $$;
