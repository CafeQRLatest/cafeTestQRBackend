-- V1_43: Create restaurant_tables table for Table Management
CREATE TABLE IF NOT EXISTS restaurant_tables (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID,
    org_id UUID,
    table_number VARCHAR(20) NOT NULL,
    name VARCHAR(100),
    seating_capacity INTEGER DEFAULT 4,
    floor VARCHAR(50),
    section VARCHAR(50),
    shape VARCHAR(50) DEFAULT 'SQUARE',
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    notes TEXT,
    display_order INTEGER DEFAULT 0,
    isactive VARCHAR(1) DEFAULT 'Y',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_restaurant_tables_client ON restaurant_tables(client_id);
CREATE INDEX IF NOT EXISTS idx_restaurant_tables_org ON restaurant_tables(client_id, org_id);
CREATE INDEX IF NOT EXISTS idx_restaurant_tables_status ON restaurant_tables(client_id, status);

-- Add Table Management menu
INSERT INTO menus (id, name, url, description, parent_id, isactive, created_at, updated_at)
VALUES (
  gen_random_uuid(),
  'Table Management',
  '/owner/table-management',
  'Manage restaurant tables, floor plan, and seating',
  NULL,
  'Y',
  NOW(),
  NOW()
)
ON CONFLICT DO NOTHING;

-- Assign Table Management menu to management roles
INSERT INTO role_menus (role_id, menu_id)
SELECT r.id, m.id
FROM roles r
CROSS JOIN menus m
WHERE m.url = '/owner/table-management'
  AND r.name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER')
ON CONFLICT DO NOTHING;
