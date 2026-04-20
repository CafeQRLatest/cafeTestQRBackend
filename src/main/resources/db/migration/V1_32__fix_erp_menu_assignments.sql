-- V1.28 Fix ERP and Inventory Menu Role Assignments
-- Ensures OWNER and MANAGER roles have access to all new modules

DO $$
BEGIN
    -- Grant access to all relevant roles for all ERP/Inventory menus
    -- This includes menus from V1_24 and V1_27
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id
    FROM roles r CROSS JOIN menus m
    WHERE r.name IN ('SUPER_ADMIN', 'ADMIN', 'OWNER', 'MANAGER') 
      AND r.client_id IS NULL 
      AND m.name IN (
          'Partners', 
          'Purchase Orders', 
          'Warehouses', 
          'Stock Overview', 
          'Stock Transfers', 
          'Stock Adjustments',
          'Inventory'
      )
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'V1.28 ERP Menu Role Assignments synchronized for all roles.';
END $$;
