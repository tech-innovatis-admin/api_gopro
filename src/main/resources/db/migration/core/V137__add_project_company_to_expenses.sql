-- Adds the official project-company link for contracted-company payments.
-- Existing organization_id data is legacy and intentionally not backfilled here.

ALTER TABLE IF EXISTS expenses
    ADD COLUMN IF NOT EXISTS project_company_id bigint;

ALTER TABLE IF EXISTS budget_items
    ADD COLUMN IF NOT EXISTS project_company_id bigint;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_expenses_project_company_id'
    ) THEN
        ALTER TABLE expenses
            ADD CONSTRAINT fk_expenses_project_company_id
                FOREIGN KEY (project_company_id) REFERENCES project_company (id);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_budget_items_project_company_id'
    ) THEN
        ALTER TABLE budget_items
            ADD CONSTRAINT fk_budget_items_project_company_id
                FOREIGN KEY (project_company_id) REFERENCES project_company (id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_expenses_project_company_id
    ON expenses (project_company_id);

CREATE INDEX IF NOT EXISTS idx_budget_items_project_company_id
    ON budget_items (project_company_id);
