-- V1_61: Add Document Sequences Menu Item and Assign to Admin Roles
INSERT INTO menus (id, name, url, description, parent_id, isactive, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Document Sequences',
  '/owner/sequences',
  'Configure auto-numbering rules for orders and invoices',
  NULL,
  'Y',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Assign Document Sequences menu to SUPER_ADMIN and ADMIN only
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.url = '/owner/sequences'
  AND r.name IN ('SUPER_ADMIN', 'ADMIN')
ON CONFLICT DO NOTHING;
