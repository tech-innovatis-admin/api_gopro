-- Alinha o schema de producao ao schema efetivo utilizado em localhost
-- (historico core + ajustes recentes), sem quebrar ambientes ja existentes.

-- 1) Garantir unicidade de CNPJ em partners (como no schema base do localhost).
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_partners_cnpj'
          AND conrelid = 'partners'::regclass
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM partners
            WHERE cnpj IS NOT NULL
            GROUP BY cnpj
            HAVING COUNT(*) > 1
        ) THEN
            RAISE EXCEPTION 'Nao foi possivel aplicar uk_partners_cnpj: existem CNPJs duplicados em partners.';
        END IF;

        ALTER TABLE partners
            ADD CONSTRAINT uk_partners_cnpj UNIQUE (cnpj);
    END IF;
END $$;

-- 2) Garantir colunas opcionais em projects.
ALTER TABLE IF EXISTS projects
    ALTER COLUMN secundary_partner_id DROP NOT NULL,
    ALTER COLUMN secundary_client_id DROP NOT NULL,
    ALTER COLUMN cordinator_id DROP NOT NULL;

-- 3) Garantir data de conclusao para hierarquia meta/etapa/fase.
ALTER TABLE IF EXISTS goals
    ADD COLUMN IF NOT EXISTS data_conclusao DATE;

ALTER TABLE IF EXISTS stages
    ADD COLUMN IF NOT EXISTS data_conclusao DATE;

ALTER TABLE IF EXISTS phases
    ADD COLUMN IF NOT EXISTS data_conclusao DATE;

-- 4) Garantir avatar_url em texto e campos opcionais em peoples.
ALTER TABLE IF EXISTS peoples
    ALTER COLUMN avatar_url TYPE TEXT,
    ALTER COLUMN birth_date DROP NOT NULL,
    ALTER COLUMN zip_code DROP NOT NULL;

-- 5) Garantir created_by opcional para ambientes sem autenticacao v1.
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT table_schema, table_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND column_name = 'created_by'
          AND is_nullable = 'NO'
    LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I ALTER COLUMN created_by DROP NOT NULL',
            rec.table_schema,
            rec.table_name
        );
    END LOOP;
END $$;

-- 6) Garantir sequence e codigo de contrato para project_company.
CREATE SEQUENCE IF NOT EXISTS project_company_contract_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

SELECT setval(
    'project_company_contract_number_seq',
    COALESCE(
        (
            SELECT MAX(
                CASE
                    WHEN contract_number ~ '^CT-[0-9]{4}-[0-9]+$'
                        THEN split_part(contract_number, '-', 3)::bigint
                    ELSE 0
                END
            )
            FROM project_company
        ),
        0
    ) + 1,
    false
);

UPDATE project_company
SET contract_number = format(
    'CT-%s-%06s',
    EXTRACT(YEAR FROM COALESCE(created_at, now()))::int,
    nextval('project_company_contract_number_seq')
)
WHERE contract_number IS NULL OR btrim(contract_number) = '';

ALTER TABLE IF EXISTS project_company
    ALTER COLUMN contract_number SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_project_company_contract_number
    ON project_company (contract_number);
