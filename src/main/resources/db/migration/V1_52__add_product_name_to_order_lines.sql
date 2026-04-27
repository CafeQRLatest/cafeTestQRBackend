-- V1.52: Add denormalized product fields to order_lines for historical accuracy
ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS product_name VARCHAR(255);
ALTER TABLE order_lines ADD COLUMN IF NOT EXISTS is_packaged_good BOOLEAN DEFAULT FALSE;
