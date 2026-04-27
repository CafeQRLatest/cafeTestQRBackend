-- V1_49: Add missing table_id column to orders table
-- The Order entity has a tableId (UUID) field but the production DB only has table_number (VARCHAR).
-- This causes: PSQLException: ERROR: column o1_0.table_id does not exist

ALTER TABLE orders ADD COLUMN IF NOT EXISTS table_id UUID;
