-- =============================================
-- V1_55: Add updated_by globally
-- =============================================

DO $$
DECLARE
    t_name TEXT;
BEGIN
    FOR t_name IN (
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
          AND table_type = 'BASE TABLE'
          AND table_name != 'flyway_schema_history'
    ) LOOP
        BEGIN
            EXECUTE format('ALTER TABLE %I ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);', t_name);
        EXCEPTION
            WHEN others THEN
                RAISE NOTICE 'Skipping table % due to error', t_name;
        END;
    END LOOP;
END $$;
