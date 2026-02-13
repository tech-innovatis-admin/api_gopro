-- Gera contract_number automaticamente para project_company no formato:
-- CT-<ANO>-<SEQUENCIAL>
-- Exemplo: CT-2026-000001

CREATE SEQUENCE IF NOT EXISTS project_company_contract_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Ajusta o valor da sequence para continuar depois do maior sufixo numérico já existente.
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

-- Backfill para registros antigos sem contract_number.
UPDATE project_company
SET contract_number = format(
    'CT-%s-%06s',
    EXTRACT(YEAR FROM COALESCE(created_at, now()))::int,
    nextval('project_company_contract_number_seq')
)
WHERE contract_number IS NULL OR btrim(contract_number) = '';

ALTER TABLE project_company
    ALTER COLUMN contract_number SET NOT NULL;

-- Garante unicidade do código.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE schemaname = 'public'
          AND indexname = 'uq_project_company_contract_number'
    ) THEN
        CREATE UNIQUE INDEX uq_project_company_contract_number
            ON project_company (contract_number);
    END IF;
END $$;
