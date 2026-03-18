-- V1.2 Refactor Multitenancy: Standardize on client_id (UUID)
-- Remove redundant tenant_id (VARCHAR) field.

DO $$ 
BEGIN 
    ---------------------------------------------------------------------------
    -- 1. Remove tenant_id from all tables
    ---------------------------------------------------------------------------
    
    -- Terminals
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='terminals' AND column_name='tenant_id') THEN
        ALTER TABLE terminals DROP COLUMN tenant_id;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations' AND column_name='tenant_id') THEN
        ALTER TABLE organizations DROP COLUMN tenant_id;
    END IF;

    -- Roles
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='roles' AND column_name='tenant_id') THEN
        ALTER TABLE roles DROP COLUMN tenant_id;
    END IF;

    -- Audit Logs
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='audit_logs' AND column_name='tenant_id') THEN
        ALTER TABLE audit_logs DROP COLUMN tenant_id;
    END IF;
    
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='tenant_id') THEN
        ALTER TABLE users DROP COLUMN tenant_id;
    END IF;
    -- Clients (if it ever had it, standardizing on 'id' being the client/tenant marker)
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='clients' AND column_name='tenant_id') THEN
        ALTER TABLE clients DROP COLUMN tenant_id;
    END IF;

    ---------------------------------------------------------------------------
    -- 2. Ensure client_id exists in all relevant tables
    ---------------------------------------------------------------------------

    -- Users Table: Needs client_id for multi-tenancy
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='users' AND column_name='client_id') THEN
        ALTER TABLE users ADD COLUMN client_id UUID REFERENCES clients(id);
    END IF;

    -- Terminals Table: Ensure both client_id and org_id exist for isolation and branch assignment
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='terminals' AND column_name='client_id') THEN
        ALTER TABLE terminals ADD COLUMN client_id UUID REFERENCES clients(id);
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='terminals' AND column_name='org_id') THEN
        ALTER TABLE terminals ADD COLUMN org_id UUID REFERENCES organizations(id);
    END IF;

    -- Note: organizations, roles, and audit_logs already have client_id from V1.1

END $$;
