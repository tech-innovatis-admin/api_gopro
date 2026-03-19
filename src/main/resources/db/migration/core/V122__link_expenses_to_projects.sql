ALTER TABLE IF EXISTS expenses
    ADD COLUMN IF NOT EXISTS project_id BIGINT;

UPDATE expenses e
SET project_id = COALESCE(e.project_id, i.project_id)
FROM incomes i
WHERE e.income_id = i.id
  AND e.project_id IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_expense_project_id'
    ) THEN
        ALTER TABLE expenses
            ADD CONSTRAINT fk_expense_project_id
                FOREIGN KEY (project_id) REFERENCES projects (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_expenses_project_id_is_active
    ON expenses (project_id, is_active);

ALTER TABLE IF EXISTS expenses
    ALTER COLUMN project_id SET NOT NULL;

ALTER TABLE IF EXISTS expenses
    ALTER COLUMN income_id DROP NOT NULL;
