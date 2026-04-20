-- Move Warehouses menu under Organization
DO $$
DECLARE
    org_menu_id UUID;
BEGIN
    SELECT id INTO org_menu_id FROM menus WHERE name = 'Organization' LIMIT 1;
    
    IF org_menu_id IS NOT NULL THEN
        UPDATE menus SET parent_id = org_menu_id WHERE name = 'Warehouses';  
    END IF;
END $$;
 