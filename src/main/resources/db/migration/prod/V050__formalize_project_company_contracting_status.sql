-- Converts project_company.status from numeric legacy codes to explicit enum values.
-- Rollback strategy: convert known enum values back to their legacy numeric codes before downgrading.

ALTER TABLE project_company
    ALTER COLUMN status TYPE varchar(30)
    USING CASE
        WHEN status IS NULL THEN 'EM_CADASTRO'
        WHEN status::text = '0' THEN 'EM_CADASTRO'
        WHEN status::text = '1' THEN 'EM_CONTRATACAO'
        WHEN status::text = '2' THEN 'CONTRATADA'
        WHEN status::text = '3' THEN 'EM_EXECUCAO'
        WHEN status::text = '4' THEN 'CONCLUIDA'
        WHEN status::text = '5' THEN 'CANCELADA'
        WHEN status::text IN (
            'EM_CADASTRO',
            'EM_CONTRATACAO',
            'CONTRATADA',
            'EM_EXECUCAO',
            'CONCLUIDA',
            'CANCELADA'
        ) THEN status::text
        ELSE 'EM_CADASTRO'
    END;

ALTER TABLE project_company
    ALTER COLUMN status SET DEFAULT 'EM_CADASTRO';

UPDATE project_company
SET status = 'EM_CADASTRO'
WHERE status IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_project_company_status'
    ) THEN
        ALTER TABLE project_company
            ADD CONSTRAINT chk_project_company_status
                CHECK (status IN (
                    'EM_CADASTRO',
                    'EM_CONTRATACAO',
                    'CONTRATADA',
                    'EM_EXECUCAO',
                    'CONCLUIDA',
                    'CANCELADA'
                ));
    END IF;
END
$$;
