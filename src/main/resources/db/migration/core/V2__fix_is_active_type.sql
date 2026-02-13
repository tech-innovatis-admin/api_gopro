DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'organizations'
          AND column_name = 'is_active'
          AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE organizations
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;
