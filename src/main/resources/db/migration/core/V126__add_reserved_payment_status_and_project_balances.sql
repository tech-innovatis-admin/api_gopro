ALTER TABLE IF EXISTS projects
    ADD COLUMN IF NOT EXISTS total_reserved NUMERIC(35,2) NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS projects
    ADD COLUMN IF NOT EXISTS saldo_real NUMERIC(35,2) NOT NULL DEFAULT 0;

ALTER TABLE IF EXISTS expenses
    ADD COLUMN IF NOT EXISTS payment_status VARCHAR(30) NOT NULL DEFAULT 'PAGO';

CREATE INDEX IF NOT EXISTS idx_expenses_project_payment_status_is_active
    ON expenses (project_id, payment_status, is_active);

UPDATE budget_items bi
SET executed_amount = COALESCE((
    SELECT SUM(e.amount)
    FROM expenses e
    WHERE e.budget_item_id = bi.id
      AND e.is_active = true
), 0);

UPDATE projects p
SET total_received = COALESCE((
        SELECT SUM(i.amount)
        FROM incomes i
        WHERE i.project_id = p.id
          AND i.is_active = true
    ), 0),
    total_expenses = COALESCE((
        SELECT SUM(e.amount)
        FROM expenses e
        WHERE e.project_id = p.id
          AND e.is_active = true
          AND e.payment_status = 'PAGO'
    ), 0),
    total_reserved = COALESCE((
        SELECT SUM(e.amount)
        FROM expenses e
        WHERE e.project_id = p.id
          AND e.is_active = true
          AND e.payment_status = 'RESERVADO'
    ), 0);

UPDATE projects
SET saldo_real = total_received - total_expenses,
    saldo = total_received - total_expenses - total_reserved;
