-- V1.10 Add created_by column to products and categories
DO $$ 
BEGIN 
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='products' AND column_name='created_by') THEN
        ALTER TABLE products ADD COLUMN created_by VARCHAR(255);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='categories' AND column_name='created_by') THEN
        ALTER TABLE categories ADD COLUMN created_by VARCHAR(255);
    END IF;
END $$;
