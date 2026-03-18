-- V1.3 Repair Orphaned Data: Systematically re-link users, organizations, and terminals to client_id
DO $$ 
BEGIN 
    -- 1. Link Users to Clients by email
    -- This restores the primary link for account owners
    UPDATE users u 
    SET client_id = c.id 
    FROM clients c 
    WHERE u.email = c.email AND u.client_id IS NULL;

    -- 2. Link Organizations to Clients
    -- Match by organization email or by matching the creator's client_id
    UPDATE organizations o
    SET client_id = c.id
    FROM clients c
    WHERE (o.email = c.email OR c.id = (SELECT client_id FROM users WHERE email = o.email LIMIT 1))
    AND o.client_id IS NULL;

    -- 3. Link Terminals to Clients via Organization ownership
    -- Since terminals are assigned to organizations (branches), we inherit the client_id
    -- This requires the organization to have been linked in step 2.
    UPDATE terminals t
    SET client_id = o.client_id
    FROM organizations o
    WHERE t.org_id = o.id
    AND t.client_id IS NULL
    AND o.client_id IS NOT NULL;

    -- 4. Link Audit Logs via User ownership
    UPDATE audit_logs a
    SET client_id = u.client_id
    FROM users u
    WHERE a.user_id = u.id
    AND a.client_id IS NULL
    AND u.client_id IS NOT NULL;

    -- 5. Final Fallback: If a single client exists, assign remaining orphaned records
    -- (Safety net for data entered without emails)
    IF (SELECT COUNT(*) FROM clients) = 1 THEN
        UPDATE users SET client_id = (SELECT id FROM clients LIMIT 1) WHERE client_id IS NULL;
        UPDATE organizations SET client_id = (SELECT id FROM clients LIMIT 1) WHERE client_id IS NULL;
        UPDATE terminals SET client_id = (SELECT id FROM clients LIMIT 1) WHERE client_id IS NULL;
        UPDATE roles SET client_id = (SELECT id FROM clients LIMIT 1) WHERE client_id IS NULL;
        UPDATE audit_logs SET client_id = (SELECT id FROM clients LIMIT 1) WHERE client_id IS NULL;
    END IF;

END $$;
