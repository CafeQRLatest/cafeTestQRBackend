-- V1.35 Repair Missing ERP Schema
-- Re-applying tables from V1.27 which were skipped because the current version was 1.33.

DO $$
BEGIN
    -- 1. Warehouses
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'warehouses') THEN
        CREATE TABLE warehouses (
            id UUID PRIMARY KEY,
            organization_id UUID NOT NULL,
            code VARCHAR(50) NOT NULL,
            name VARCHAR(100) NOT NULL,
            is_active BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255)
        );
        CREATE UNIQUE INDEX idx_warehouses_code_org ON warehouses(code, organization_id);
    END IF;

    -- 2. Add warehouse_id to orders
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='orders' AND column_name='warehouse_id') THEN
        ALTER TABLE orders ADD COLUMN warehouse_id UUID REFERENCES warehouses(id);
    END IF;

    -- 3. Stock Snapshots (Quick lookup)
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'stock_snapshots') THEN
        CREATE TABLE stock_snapshots (
            id UUID PRIMARY KEY,
            product_id UUID NOT NULL REFERENCES products(id),
            warehouse_id UUID NOT NULL REFERENCES warehouses(id),
            quantity DECIMAL(19, 4) DEFAULT 0,
            reserved_quantity DECIMAL(19, 4) DEFAULT 0,
            uom_id UUID NOT NULL,
            last_updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
        );
        CREATE UNIQUE INDEX idx_stock_product_warehouse ON stock_snapshots(product_id, warehouse_id);
    END IF;

    -- 4. Stock Ledgers (Audit log)
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'stock_ledgers') THEN
        CREATE TABLE stock_ledgers (
            id UUID PRIMARY KEY,
            product_id UUID NOT NULL REFERENCES products(id),
            warehouse_id UUID NOT NULL REFERENCES warehouses(id),
            transaction_type VARCHAR(50) NOT NULL, -- PURCHASE, SALE, TRANSFER, ADJUSTMENT
            delta_quantity DECIMAL(19, 4) NOT NULL,
            balance_after DECIMAL(19, 4) NOT NULL,
            reference_type VARCHAR(50), -- ORDER, TRANSFER, ADJUSTMENT
            reference_id UUID,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255)
        );
        CREATE INDEX idx_ledger_product_warehouse ON stock_ledgers(product_id, warehouse_id);
    END IF;

    -- 5. Stock Transfers
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'stock_transfers') THEN
        CREATE TABLE stock_transfers (
            id UUID PRIMARY KEY,
            from_warehouse_id UUID NOT NULL REFERENCES warehouses(id),
            to_warehouse_id UUID NOT NULL REFERENCES warehouses(id),
            status VARCHAR(50) NOT NULL, -- DRAFT, SHIPPED, RECEIVED, CANCELLED
            transfer_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            notes TEXT,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255)
        );
    END IF;

    -- 6. Stock Adjustments
    IF NOT EXISTS (SELECT 1 FROM pg_tables WHERE tablename = 'stock_adjustments') THEN
        CREATE TABLE stock_adjustments (
            id UUID PRIMARY KEY,
            warehouse_id UUID NOT NULL REFERENCES warehouses(id),
            reason_code VARCHAR(100),
            notes TEXT,
            adjustment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
            created_by VARCHAR(255),
            updated_by VARCHAR(255)
        );
    END IF;
END $$;
