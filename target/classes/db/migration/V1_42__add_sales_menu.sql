-- V1_42: Add Sales POS menu item
INSERT INTO menus (id, name, url, description, parent_id, isactive, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Sales',
  '/owner/sales',
  'Sales Order POS Screen',
  NULL,
  'Y',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Assign Sales menu to all roles
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.url = '/owner/sales'
  AND r.name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'STAFF')
ON CONFLICT DO NOTHING;
