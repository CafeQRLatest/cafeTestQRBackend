-- V1.22 Add Print and Receipt Settings to Global Configurations
-- Adds columns for Receipt Footer, Logo Bitmap, and Hardware/Paper formatting.

-- Add columns to system_configurations if they don't exist
ALTER TABLE system_configurations 
ADD COLUMN IF NOT EXISTS bill_footer TEXT,
ADD COLUMN IF NOT EXISTS print_logo_bitmap TEXT,
ADD COLUMN IF NOT EXISTS print_logo_cols INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS print_logo_rows INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS paper_mm VARCHAR(10) DEFAULT '58',
ADD COLUMN IF NOT EXISTS print_cols INTEGER DEFAULT 32,
ADD COLUMN IF NOT EXISTS print_left_margin_dots INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS print_right_margin_dots INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS print_auto_cut BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS print_win_list_url VARCHAR(255) DEFAULT 'http://127.0.0.1:3333/printers',
ADD COLUMN IF NOT EXISTS print_win_post_url VARCHAR(255) DEFAULT 'http://127.0.0.1:3333/printRaw';
