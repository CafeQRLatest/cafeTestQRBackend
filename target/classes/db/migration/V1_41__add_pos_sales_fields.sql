-- V1_41: Add fulfillment_type and table_number to orders for POS Sales flow
ALTER TABLE orders ADD COLUMN IF NOT EXISTS fulfillment_type VARCHAR(20) DEFAULT 'DINE_IN';
ALTER TABLE orders ADD COLUMN IF NOT EXISTS table_number VARCHAR(20);
