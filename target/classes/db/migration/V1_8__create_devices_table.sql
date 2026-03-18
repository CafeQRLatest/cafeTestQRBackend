-- Create devices table
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    org_id UUID,
    name VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL, -- e.g. 'PRINTER', 'SCANNER', 'TABLET', 'DESKTOP'
    serial_number VARCHAR(100),
    isactive VARCHAR(1) DEFAULT 'Y',
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Add device_id to terminals table
ALTER TABLE terminals ADD COLUMN device_id UUID;

-- Add foreign key constraint
ALTER TABLE terminals 
ADD CONSTRAINT fk_terminals_device 
FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL;
