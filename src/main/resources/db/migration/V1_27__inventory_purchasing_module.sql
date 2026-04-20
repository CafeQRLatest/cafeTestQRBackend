-- V1.27 Simple Inventory and Purchasing Module
-- Creates Warehouses, Stock Ledgers, Transfers, and Adjustments (No GRN, direct PO-to-Stock)

DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Warehouses (Master Configuration)
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS warehouses (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        name VARCHAR(150) NOT NULL,
        code VARCHAR(50),
        address TEXT,
        manager_name VARCHAR(100),
        manager_phone VARCHAR(50),
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    ---------------------------------------------------------------------------
    -- 2. Stock Transfers (Inter-Warehouse)
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS stock_transfers (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        source_warehouse_id UUID REFERENCES warehouses(id) NOT NULL,
        dest_warehouse_id UUID REFERENCES warehouses(id) NOT NULL,
        transfer_number VARCHAR(50) UNIQUE NOT NULL,
        transfer_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, IN_TRANSIT, COMPLETED, CANCELLED
        notes TEXT,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    CREATE TABLE IF NOT EXISTS stock_transfer_lines (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        transfer_id UUID REFERENCES stock_transfers(id) ON DELETE CASCADE,
        product_id UUID NOT NULL,
        variant_id UUID,
        transfer_quantity DECIMAL(15, 3) NOT NULL,
        isactive CHAR(1) DEFAULT 'Y'
    );

    ---------------------------------------------------------------------------
    -- 3. Stock Adjustments (Wastage / Audit Corrections)
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS stock_adjustments (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        warehouse_id UUID REFERENCES warehouses(id) NOT NULL,
        adjustment_number VARCHAR(50) UNIQUE NOT NULL,
        adjustment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        reason VARCHAR(50) NOT NULL, -- WASTAGE, DAMAGE, AUDIT, EXPIRY
        status VARCHAR(20) DEFAULT 'DRAFT', -- DRAFT, COMPLETED
        notes TEXT,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    CREATE TABLE IF NOT EXISTS stock_adjustment_lines (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        adjustment_id UUID REFERENCES stock_adjustments(id) ON DELETE CASCADE,
        product_id UUID NOT NULL,
        variant_id UUID,
        quantity_change DECIMAL(15, 3) NOT NULL, -- Can be negative or positive
        unit_cost DECIMAL(15, 2) DEFAULT 0,
        isactive CHAR(1) DEFAULT 'Y'
    );

    ---------------------------------------------------------------------------
    -- 4. Stock Ledgers (The Immutable Truth) & Snapshots
    ---------------------------------------------------------------------------
    CREATE TABLE IF NOT EXISTS stock_ledgers (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        warehouse_id UUID REFERENCES warehouses(id) NOT NULL,
        product_id UUID NOT NULL,
        variant_id UUID,
        transaction_type VARCHAR(20) NOT NULL, -- PURCHASE, SALE, TRANSFER_IN, TRANSFER_OUT, ADJUSTMENT
        reference_id UUID, -- ID pointing to orders (Sale/Purchase), Transfer, or Adjustment
        quantity_change DECIMAL(15, 3) NOT NULL,
        balance_after_transaction DECIMAL(15, 3) NOT NULL,
        unit_cost DECIMAL(15, 2) DEFAULT 0,
        transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID
    );

    -- Caching table for real-time balances
    CREATE TABLE IF NOT EXISTS stock_snapshots (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        warehouse_id UUID REFERENCES warehouses(id) NOT NULL,
        product_id UUID NOT NULL,
        variant_id UUID,
        current_quantity DECIMAL(15, 3) DEFAULT 0,
        last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        UNIQUE(warehouse_id, product_id, variant_id)
    );

    ---------------------------------------------------------------------------
    -- 5. Insert Menus for ERP
    ---------------------------------------------------------------------------
    -- Warehouse Master inside Organization
    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Warehouses', '/admin/warehouses', 'Manage Stock Locations', 'Y')
    ON CONFLICT DO NOTHING;

    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Stock Overview', '/owner/stock-overview', 'Real-time Balances', 'Y')
    ON CONFLICT DO NOTHING;

    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Stock Transfers', '/owner/stock-transfers', 'Move Stock', 'Y')
    ON CONFLICT DO NOTHING;

    INSERT INTO menus (id, name, url, description, isactive)
    VALUES (gen_random_uuid(), 'Stock Adjustments', '/owner/stock-adjustments', 'Audit & Wastage', 'Y')
    ON CONFLICT DO NOTHING;

    -- Grant full access to SUPER_ADMIN and ADMIN
    INSERT INTO role_menus (role_id, menu_id)
    SELECT r.id, m.id
    FROM roles r CROSS JOIN menus m
    WHERE r.name IN ('SUPER_ADMIN', 'ADMIN') 
      AND r.client_id IS NULL 
      AND m.name IN ('Warehouses', 'Stock Overview', 'Stock Transfers', 'Stock Adjustments')
    ON CONFLICT DO NOTHING;

    RAISE NOTICE 'V1.27 Simplified Inventory module tables applied successfully!';

    ---------------------------------------------------------------------------
    -- 6. Link Orders to Warehouses
    ---------------------------------------------------------------------------
    ALTER TABLE orders ADD COLUMN IF NOT EXISTS warehouse_id UUID REFERENCES warehouses(id);

END $$;
