-- V1.23 ERP Core Module (Clean Slate Version)
-- This version drops any existing conflicting tables to ensure a 100% clean schema that matches the new ERP design.
-- WARNING: This will remove any existing data in these tables.

DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 0. Clean Sate: Drop existing conflicting tables
    ---------------------------------------------------------------------------
    DROP TABLE IF EXISTS payments CASCADE;
    DROP TABLE IF EXISTS invoice_lines CASCADE;
    DROP TABLE IF EXISTS invoices CASCADE;
    DROP TABLE IF EXISTS order_lines CASCADE;
    DROP TABLE IF EXISTS orders CASCADE;
    DROP TABLE IF EXISTS product_purchase_prices CASCADE;
    DROP TABLE IF EXISTS product_prices CASCADE;
    DROP TABLE IF EXISTS pricelist_versions CASCADE;
    DROP TABLE IF EXISTS pricelists CASCADE;
    DROP TABLE IF EXISTS vendors CASCADE;
    DROP TABLE IF EXISTS customers CASCADE;
    DROP TABLE IF EXISTS currencies CASCADE;

    ---------------------------------------------------------------------------
    -- 1. Master Data: Customers & Vendors
    ---------------------------------------------------------------------------

    -- Currencies
    CREATE TABLE currencies (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        code VARCHAR(10) NOT NULL,
        symbol VARCHAR(10) NOT NULL,
        name VARCHAR(100) NOT NULL,
        exchange_rate DECIMAL(18, 6) DEFAULT 1.0,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Price Lists
    CREATE TABLE pricelists (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        terminal_id UUID REFERENCES terminals(id),
        pricelist_type VARCHAR(20) NOT NULL, -- SALE, PURCHASE
        name VARCHAR(150) NOT NULL,
        currency_id UUID REFERENCES currencies(id),
        is_default BOOLEAN DEFAULT FALSE,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Price List Versions
    CREATE TABLE pricelist_versions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        pricelist_id UUID REFERENCES pricelists(id) ON DELETE CASCADE,
        name VARCHAR(150) NOT NULL,
        start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        end_date TIMESTAMP,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Customers
    CREATE TABLE customers (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        pricelist_id UUID REFERENCES pricelists(id),
        name VARCHAR(200) NOT NULL,
        phone VARCHAR(50),
        email VARCHAR(255),
        address TEXT,
        gst_number VARCHAR(50),
        customer_category VARCHAR(50) DEFAULT 'REGULAR',
        loyalty_points INTEGER DEFAULT 0,
        credit_limit DECIMAL(15, 2) DEFAULT 0,
        opening_balance DECIMAL(15, 2) DEFAULT 0,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Vendors
    CREATE TABLE vendors (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        pricelist_id UUID REFERENCES pricelists(id),
        name VARCHAR(200) NOT NULL,
        contact_person VARCHAR(150),
        phone VARCHAR(50),
        email VARCHAR(255),
        address TEXT,
        gstin VARCHAR(50),
        opening_balance DECIMAL(15, 2) DEFAULT 0,
        credit_limit DECIMAL(15, 2) DEFAULT 0,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    ---------------------------------------------------------------------------
    -- 2. Pricing Layer (Selling vs Buying)
    ---------------------------------------------------------------------------

    -- Selling Prices
    CREATE TABLE product_prices (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        pricelist_version_id UUID REFERENCES pricelist_versions(id) ON DELETE CASCADE,
        product_id UUID,
        variant_id UUID,
        price DECIMAL(15, 2) NOT NULL,
        discount_percent DECIMAL(5, 2) DEFAULT 0,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Buying Prices
    CREATE TABLE product_purchase_prices (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        pricelist_version_id UUID REFERENCES pricelist_versions(id) ON DELETE CASCADE,
        vendor_id UUID REFERENCES vendors(id),
        product_id UUID,
        variant_id UUID,
        purchase_price DECIMAL(15, 2) NOT NULL,
        min_quantity DECIMAL(15, 3) DEFAULT 0,
        lead_time_days INTEGER DEFAULT 0,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    ---------------------------------------------------------------------------
    -- 3. Transactional Infrastructure (Orders & Invoicing)
    ---------------------------------------------------------------------------

    -- Orders
    CREATE TABLE orders (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        terminal_id UUID REFERENCES terminals(id),
        order_no VARCHAR(50) UNIQUE NOT NULL,
        order_type VARCHAR(20) NOT NULL, -- SALE, PURCHASE
        order_status VARCHAR(20) DEFAULT 'DRAFT', 
        payment_status VARCHAR(20) DEFAULT 'PENDING',
        order_source VARCHAR(50) DEFAULT 'OFFLINE',
        customer_id UUID REFERENCES customers(id),
        vendor_id UUID REFERENCES vendors(id),
        pricelist_id UUID REFERENCES pricelists(id),
        currency_id UUID REFERENCES currencies(id),
        order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        total_tax_amount DECIMAL(15, 2) DEFAULT 0,
        total_discount_amount DECIMAL(15, 2) DEFAULT 0,
        total_amount DECIMAL(15, 2) DEFAULT 0,
        grand_total DECIMAL(15, 2) DEFAULT 0,
        description TEXT,
        reference VARCHAR(100),
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Order Lines
    CREATE TABLE order_lines (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        order_id UUID REFERENCES orders(id) ON DELETE CASCADE,
        product_id UUID,
        variant_id UUID,
        quantity DECIMAL(15, 3) NOT NULL DEFAULT 1,
        unit_of_measure VARCHAR(20) DEFAULT 'units',
        unit_price DECIMAL(15, 2) NOT NULL,
        tax_rate DECIMAL(5, 2) DEFAULT 0,
        tax_amount DECIMAL(15, 2) DEFAULT 0,
        discount_amount DECIMAL(15, 2) DEFAULT 0,
        line_total DECIMAL(15, 2) NOT NULL,
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Invoices
    CREATE TABLE invoices (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        terminal_id UUID REFERENCES terminals(id),
        order_id UUID REFERENCES orders(id),
        customer_id UUID REFERENCES customers(id),
        vendor_id UUID REFERENCES vendors(id),
        invoice_no VARCHAR(50) UNIQUE NOT NULL,
        invoice_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        due_date TIMESTAMP,
        status VARCHAR(20) DEFAULT 'UNPAID',
        is_paid BOOLEAN DEFAULT FALSE,
        total_amount DECIMAL(15, 2) NOT NULL,
        amount_due DECIMAL(15, 2) NOT NULL,
        description TEXT,
        reference VARCHAR(100),
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Invoice Lines
    CREATE TABLE invoice_lines (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        invoice_id UUID REFERENCES invoices(id) ON DELETE CASCADE,
        order_line_id UUID REFERENCES order_lines(id),
        quantity DECIMAL(15, 3) NOT NULL,
        unit_price DECIMAL(15, 2) NOT NULL,
        line_total DECIMAL(15, 2) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    -- Payments
    CREATE TABLE payments (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        terminal_id UUID REFERENCES terminals(id),
        order_id UUID REFERENCES orders(id),
        invoice_id UUID REFERENCES invoices(id),
        payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        payment_method VARCHAR(50) NOT NULL,
        amount_paid DECIMAL(15, 2) NOT NULL,
        change_given DECIMAL(15, 2) DEFAULT 0,
        reference_no VARCHAR(100),
        description TEXT,
        status VARCHAR(20) DEFAULT 'COMPLETED',
        isactive CHAR(1) DEFAULT 'Y',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        created_by UUID,
        updated_by UUID
    );

    ---------------------------------------------------------------------------
    -- 4. Finalize Indices
    ---------------------------------------------------------------------------
    CREATE INDEX idx_orders_customer ON orders(customer_id);
    CREATE INDEX idx_orders_vendor ON orders(vendor_id);
    CREATE INDEX idx_invoices_customer ON invoices(customer_id);
    CREATE INDEX idx_invoices_vendor ON invoices(vendor_id);

END $$;
