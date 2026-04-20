-- V1_44__add_pos_product_listing_config.sql
ALTER TABLE system_configurations ADD COLUMN pos_product_listing_enabled BOOLEAN DEFAULT TRUE;
