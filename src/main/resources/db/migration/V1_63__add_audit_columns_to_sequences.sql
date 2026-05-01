-- V1_63: Add missing audit columns to document_sequences table
ALTER TABLE document_sequences ADD COLUMN IF NOT EXISTS created_by VARCHAR(255);
ALTER TABLE document_sequences ADD COLUMN IF NOT EXISTS updated_by VARCHAR(255);
