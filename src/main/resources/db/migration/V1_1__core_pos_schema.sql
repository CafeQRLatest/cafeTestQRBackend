-- V1.1 Core POS & Multi-Tenant Schema
-- Consolidate all foundational changes for Clients, Organizations, Terminals, Users, Roles, and Permissions.
-- Standardizes status to 'isactive' CHAR(1) with 'Y'/'N' values.

DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Clients Table Enhancement (Tenant Level)
    ---------------------------------------------------------------------------
    -- Cleanup legacy status if exists
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='status') THEN
        ALTER TABLE clients DROP COLUMN status;
    END IF;

    -- Standard isactive
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='isactive') THEN
        ALTER TABLE clients ADD COLUMN isactive CHAR(1) DEFAULT 'Y';
    END IF;

    -- Registration & Identity
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='legal_name') THEN
        ALTER TABLE clients ADD COLUMN legal_name VARCHAR(200);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='owner_name') THEN
        ALTER TABLE clients ADD COLUMN owner_name VARCHAR(150);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='pan_number') THEN
        ALTER TABLE clients ADD COLUMN pan_number VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='gst_number') THEN
        ALTER TABLE clients ADD COLUMN gst_number VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='fssai_number') THEN
        ALTER TABLE clients ADD COLUMN fssai_number VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='address') THEN
        ALTER TABLE clients ADD COLUMN address TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='website') THEN
        ALTER TABLE clients ADD COLUMN website VARCHAR(255);
    END IF;

    -- Branding & Ops
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='logo_url') THEN
        ALTER TABLE clients ADD COLUMN logo_url TEXT;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='brand_color') THEN
        ALTER TABLE clients ADD COLUMN brand_color VARCHAR(20);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='currency') THEN
        ALTER TABLE clients ADD COLUMN currency VARCHAR(20) DEFAULT 'INR';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='timezone') THEN
        ALTER TABLE clients ADD COLUMN timezone VARCHAR(100);
    END IF;

    -- Social & Location
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='google_maps_url') THEN
        ALTER TABLE clients ADD COLUMN google_maps_url VARCHAR(1024);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='pin_code') THEN
        ALTER TABLE clients ADD COLUMN pin_code VARCHAR(20);
    END IF;

    -- Social & Contact Extras
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='instagram_url') THEN
        ALTER TABLE clients ADD COLUMN instagram_url VARCHAR(512);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='facebook_url') THEN
        ALTER TABLE clients ADD COLUMN facebook_url VARCHAR(512);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='whatsapp_number') THEN
        ALTER TABLE clients ADD COLUMN whatsapp_number VARCHAR(50);
    END IF;

    -- Bank Details
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='bank_name') THEN
        ALTER TABLE clients ADD COLUMN bank_name VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='account_number') THEN
        ALTER TABLE clients ADD COLUMN account_number VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='ifsc_code') THEN
        ALTER TABLE clients ADD COLUMN ifsc_code VARCHAR(255);
    END IF;

    ---------------------------------------------------------------------------
    -- 2. Organizations Table Enhancement (Branch Level)
    ---------------------------------------------------------------------------
    -- Linkage & Identity
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='client_id') THEN
        ALTER TABLE organizations ADD COLUMN client_id UUID REFERENCES clients(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='org_code') THEN
        ALTER TABLE organizations ADD COLUMN org_code VARCHAR(50);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='email') THEN
        ALTER TABLE organizations ADD COLUMN email VARCHAR(255);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='gstin') THEN
        ALTER TABLE organizations ADD COLUMN gstin VARCHAR(255);
    END IF;

    -- Location & Delivery
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='latitude') THEN
        ALTER TABLE organizations ADD COLUMN latitude DOUBLE PRECISION;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='longitude') THEN
        ALTER TABLE organizations ADD COLUMN longitude DOUBLE PRECISION;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='delivery_radius_km') THEN
        ALTER TABLE organizations ADD COLUMN delivery_radius_km DOUBLE PRECISION;
    END IF;
    
    -- Status Management (Standardized)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='status') THEN
        ALTER TABLE organizations DROP COLUMN status;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='active') THEN
        ALTER TABLE organizations DROP COLUMN active;
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='is_active') THEN
        ALTER TABLE organizations DROP COLUMN is_active;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='isactive') THEN
        ALTER TABLE organizations ADD COLUMN isactive CHAR(1) DEFAULT 'Y';
    END IF;

    -- Branding
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='logo_url') THEN
        ALTER TABLE organizations ADD COLUMN logo_url TEXT;
    END IF;

    ---------------------------------------------------------------------------
    -- 3. Core Master Tables (Terminals, Roles, Permissions)
    ---------------------------------------------------------------------------
    -- Terminals
    CREATE TABLE IF NOT EXISTS terminals (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        org_id UUID REFERENCES organizations(id),
        name VARCHAR(100) NOT NULL,
        terminal_code VARCHAR(50) UNIQUE,
        device_type VARCHAR(50), 
        ip_address VARCHAR(50),
        isactive CHAR(1) DEFAULT 'Y', -- Standardized
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        tenant_id VARCHAR(255)
    );

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='terminals' AND column_name='status') THEN
        ALTER TABLE terminals DROP COLUMN status;
    END IF;

    -- Permissions
    CREATE TABLE IF NOT EXISTS permissions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(150) UNIQUE NOT NULL,
        description TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

    -- Roles
    CREATE TABLE IF NOT EXISTS roles (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID REFERENCES clients(id),
        name VARCHAR(100) NOT NULL,
        description TEXT,
        isactive CHAR(1) DEFAULT 'Y', -- Standardized
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        tenant_id VARCHAR(255)
    );

    -- Role-Permissions Mapping
    CREATE TABLE IF NOT EXISTS role_permissions (
        role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
        permission_id UUID REFERENCES permissions(id) ON DELETE CASCADE,
        PRIMARY KEY (role_id, permission_id)
    );

    ---------------------------------------------------------------------------
    -- 4. User Access & Audit
    ---------------------------------------------------------------------------
    -- Update Users
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='org_id') THEN
        ALTER TABLE users ADD COLUMN org_id UUID REFERENCES organizations(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='terminal_id') THEN
        ALTER TABLE users ADD COLUMN terminal_id UUID REFERENCES terminals(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='role_id') THEN
        ALTER TABLE users ADD COLUMN role_id UUID REFERENCES roles(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='phone') THEN
        ALTER TABLE users ADD COLUMN phone VARCHAR(50);
    END IF;
    
    -- User IsActive
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='isactive') THEN
        ALTER TABLE users ADD COLUMN isactive CHAR(1) DEFAULT 'Y';
    END IF;

    -- User Terminal Access Mapping
    CREATE TABLE IF NOT EXISTS user_terminal_access (
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        terminal_id UUID REFERENCES terminals(id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, terminal_id)
    );

    -- User Organization Access Mapping
    CREATE TABLE IF NOT EXISTS user_org_access (
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        org_id UUID REFERENCES organizations(id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, org_id)
    );

    -- Audit Logs
    CREATE TABLE IF NOT EXISTS audit_logs (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        client_id UUID,
        org_id UUID,
        terminal_id UUID,
        user_id UUID,
        action VARCHAR(255),
        entity_name VARCHAR(100),
        entity_id VARCHAR(100),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        ip_address VARCHAR(50),
        tenant_id VARCHAR(255)
    );

    -- Seed Default Permissions
    INSERT INTO permissions (name, description) VALUES 
    ('CREATE_ORDER', 'Allow creating new orders'),
    ('VOID_BILL', 'Allow voiding paid bills'),
    ('VIEW_REPORT', 'Allow viewing sales reports'),
    ('MANAGE_USERS', 'Allow managing staff accounts'),
    ('MANAGE_ORG', 'Allow managing branch details'),
    ('MANAGE_TERMINAL', 'Allow managing POS devices')
    ON CONFLICT (name) DO NOTHING;

END $$;
