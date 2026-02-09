DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'companies' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE companies
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'organizations' AND column_name = 'is_active' AND data_type <> 'boolean'
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

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'partners' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE partners
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'public_agencies' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE public_agencies
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'secretariats' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE secretariats
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'peoples' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE peoples
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'projects' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE projects
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'goals' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE goals
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'stages' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE stages
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'phases' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE phases
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'budget_categories' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE budget_categories
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'budget_items' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE budget_items
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'budget_transfers' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE budget_transfers
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'disbursement_schedule' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE disbursement_schedule
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'incomes' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE incomes
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'expenses' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE expenses
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'project_people' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE project_people
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'project_company' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE project_company
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'documents' AND column_name = 'is_active' AND data_type <> 'boolean'
    ) THEN
        ALTER TABLE documents
            ALTER COLUMN is_active TYPE boolean
            USING CASE
                WHEN is_active IS NULL THEN false
                WHEN is_active::text IN ('1', 't', 'true', 'y', 'yes') THEN true
                ELSE false
            END;
    END IF;
END $$;
