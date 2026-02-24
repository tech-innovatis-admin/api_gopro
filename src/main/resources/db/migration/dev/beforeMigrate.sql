-- Dev safeguard for baselined/non-empty databases.
-- Some legacy schemas drift from the shape expected by V100/V101 seeds.
DO $$
BEGIN
    -- Run only while V100/V101 are still pending.
    IF to_regclass('public.flyway_schema_history') IS NULL
       OR NOT EXISTS (
            SELECT 1
            FROM flyway_schema_history
            WHERE success = TRUE
              AND version IN ('100', '101')
        ) THEN
        -- Base seed columns used by V100/V101.
        ALTER TABLE IF EXISTS partners
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS public_agencies
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS secretariats
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS peoples
            ADD COLUMN IF NOT EXISTS avatar_url VARCHAR(500),
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS zip_code VARCHAR(20),
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS organizations
            ADD COLUMN IF NOT EXISTS trade_name VARCHAR(255),
            ADD COLUMN IF NOT EXISTS type SMALLINT,
            ADD COLUMN IF NOT EXISTS contact_person VARCHAR(255),
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS companies
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS projects
            ADD COLUMN IF NOT EXISTS project_status VARCHAR(50),
            ADD COLUMN IF NOT EXISTS "object" TEXT,
            ADD COLUMN IF NOT EXISTS primary_partner_id BIGINT,
            ADD COLUMN IF NOT EXISTS secundary_partner_id BIGINT,
            ADD COLUMN IF NOT EXISTS primary_client_id BIGINT,
            ADD COLUMN IF NOT EXISTS secundary_client_id BIGINT,
            ADD COLUMN IF NOT EXISTS cordinator_id BIGINT,
            ADD COLUMN IF NOT EXISTS project_gov_if VARCHAR(50),
            ADD COLUMN IF NOT EXISTS project_type VARCHAR(50),
            ADD COLUMN IF NOT EXISTS closing_date DATE,
            ADD COLUMN IF NOT EXISTS city VARCHAR(255),
            ADD COLUMN IF NOT EXISTS state VARCHAR(255),
            ADD COLUMN IF NOT EXISTS total_received NUMERIC(35,2) NOT NULL DEFAULT 0,
            ADD COLUMN IF NOT EXISTS total_expenses NUMERIC(35,2) NOT NULL DEFAULT 0,
            ADD COLUMN IF NOT EXISTS saldo NUMERIC(35,2) NOT NULL DEFAULT 0,
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        -- Legacy required columns that are not present in V100/V101 inserts.
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'projects'
              AND column_name = 'executing_org_id'
        ) THEN
            ALTER TABLE projects
                ALTER COLUMN executing_org_id DROP NOT NULL;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'projects'
              AND column_name = 'orgao_financiador_id'
        ) THEN
            ALTER TABLE projects
                ALTER COLUMN orgao_financiador_id DROP NOT NULL;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'projects'
              AND column_name = 'status_projects'
        ) THEN
            ALTER TABLE projects
                ALTER COLUMN status_projects DROP NOT NULL;
        END IF;

        ALTER TABLE IF EXISTS goals
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS stages
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS phases
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS budget_categories
            ADD COLUMN IF NOT EXISTS project_id BIGINT,
            ADD COLUMN IF NOT EXISTS code VARCHAR(50),
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS budget_items
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS incomes
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS disbursement_schedule
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS documents
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS expenses
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS budget_transfers
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        ALTER TABLE IF EXISTS project_people
            ADD COLUMN IF NOT EXISTS person_id BIGINT,
            ADD COLUMN IF NOT EXISTS role VARCHAR(255),
            ADD COLUMN IF NOT EXISTS workload_hours NUMERIC(5,2),
            ADD COLUMN IF NOT EXISTS institutional_link VARCHAR(255),
            ADD COLUMN IF NOT EXISTS contract_type VARCHAR(50),
            ADD COLUMN IF NOT EXISTS status VARCHAR(50),
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

        -- Legacy required columns that are not present in V100/V101 inserts.
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'project_people'
              AND column_name = 'people_id'
        ) THEN
            ALTER TABLE project_people
                ALTER COLUMN people_id DROP NOT NULL;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'project_people'
              AND column_name = 'role_project_people'
        ) THEN
            ALTER TABLE project_people
                ALTER COLUMN role_project_people DROP NOT NULL;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = 'project_people'
              AND column_name = 'status_project_people'
        ) THEN
            ALTER TABLE project_people
                ALTER COLUMN status_project_people DROP NOT NULL;
        END IF;

        ALTER TABLE IF EXISTS project_company
            ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
            ADD COLUMN IF NOT EXISTS created_by BIGINT,
            ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();
    END IF;
END $$;
