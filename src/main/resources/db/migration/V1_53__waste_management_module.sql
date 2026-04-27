-- V1.53: Waste Management Module
-- Waste categories (Spillage, Burnt, Expired, Customer Return, Over-prep, Other)
CREATE TABLE IF NOT EXISTS waste_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID REFERENCES clients(id),
    org_id UUID REFERENCES organizations(id),
    name VARCHAR(100) NOT NULL,
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    isactive CHAR(1) DEFAULT 'Y',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Waste log entries
CREATE TABLE IF NOT EXISTS waste_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID REFERENCES clients(id),
    org_id UUID REFERENCES organizations(id),
    terminal_id UUID REFERENCES terminals(id),
    waste_category_id UUID REFERENCES waste_categories(id),
    product_id UUID,
    product_name VARCHAR(255),
    waste_reason VARCHAR(50) NOT NULL,
    quantity DECIMAL(15, 3) NOT NULL DEFAULT 1,
    unit_of_measure VARCHAR(20) DEFAULT 'units',
    unit_cost DECIMAL(15, 2) DEFAULT 0,
    total_cost DECIMAL(15, 2) DEFAULT 0,
    notes TEXT,
    waste_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isactive CHAR(1) DEFAULT 'Y',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID
);

-- Seed default waste categories
INSERT INTO waste_categories (id, name, icon, sort_order)
SELECT gen_random_uuid(), 'Spillage', 'FaTint', 1
WHERE NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = 'Spillage');

INSERT INTO waste_categories (id, name, icon, sort_order)
SELECT gen_random_uuid(), 'Burnt / Overcooked', 'FaFire', 2
WHERE NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = 'Burnt / Overcooked');

INSERT INTO waste_categories (id, name, icon, sort_order)
SELECT gen_random_uuid(), 'Expired / Spoiled', 'FaExclamationTriangle', 3
WHERE NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = 'Expired / Spoiled');

INSERT INTO waste_categories (id, name, icon, sort_order)
SELECT gen_random_uuid(), 'Customer Return', 'FaUndo', 4
WHERE NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = 'Customer Return');

INSERT INTO waste_categories (id, name, icon, sort_order)
SELECT gen_random_uuid(), 'Over-preparation', 'FaBoxOpen', 5
WHERE NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = 'Over-preparation');

INSERT INTO waste_categories (id, name, icon, sort_order)
SELECT gen_random_uuid(), 'Other', 'FaEllipsisH', 6
WHERE NOT EXISTS (SELECT 1 FROM waste_categories WHERE name = 'Other');
