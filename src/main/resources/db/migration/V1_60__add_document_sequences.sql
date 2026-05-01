-- V1_60: Add Document Sequences table for high-concurrency safe sequence generation
CREATE TABLE IF NOT EXISTS document_sequences (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL,
    org_id UUID NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    prefix VARCHAR(20),
    suffix VARCHAR(20),
    padding_length INT NOT NULL DEFAULT 5,
    next_number BIGINT NOT NULL DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    UNIQUE(client_id, org_id, document_type)
);
