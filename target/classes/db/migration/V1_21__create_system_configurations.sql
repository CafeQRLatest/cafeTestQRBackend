-- V1.21 Create System Configurations Table (Global)
-- Stores power module toggles and round-off logic defaults for the whole system.

CREATE TABLE IF NOT EXISTS system_configurations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    
    -- Power Modules (Feature Toggles)
    online_payment_enabled BOOLEAN DEFAULT FALSE,
    menu_images_enabled BOOLEAN DEFAULT FALSE,
    credit_enabled BOOLEAN DEFAULT FALSE,
    table_management_enabled BOOLEAN DEFAULT FALSE,
    qr_ordering_enabled BOOLEAN DEFAULT TRUE,
    inventory_enabled BOOLEAN DEFAULT FALSE,
    production_enabled BOOLEAN DEFAULT FALSE,
    customers_enabled BOOLEAN DEFAULT FALSE,
    loyalty_enabled BOOLEAN DEFAULT FALSE,
    send_to_kitchen_enabled BOOLEAN DEFAULT TRUE,
    online_delivery_enabled BOOLEAN DEFAULT FALSE,
    allow_multiple_customers_per_order BOOLEAN DEFAULT FALSE,

    -- Round-off Engine
    round_off_enabled BOOLEAN DEFAULT FALSE,
    round_off_mode VARCHAR(20) DEFAULT 'automatic', -- 'automatic' or 'manual'
    round_off_auto_factor DECIMAL(10, 2) DEFAULT 1.00,
    round_off_manual_limit DECIMAL(10, 2) DEFAULT 10.00,

    -- Locale & Global Logic
    tax_label_global VARCHAR(50) DEFAULT 'GST',
    currency_symbol VARCHAR(10) DEFAULT '₹',
    currency_position VARCHAR(10) DEFAULT 'before', -- 'before' or 'after'

    -- Auditing
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Seed the single global configuration row if it doesn't exist
INSERT INTO system_configurations (id, tax_label_global, currency_symbol, qr_ordering_enabled, send_to_kitchen_enabled)
SELECT gen_random_uuid(), 'GST', '₹', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM system_configurations);
