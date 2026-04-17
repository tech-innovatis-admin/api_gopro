UPDATE expenses
SET paid_by = 'INNOVATIS'
WHERE paid_by IS NULL OR paid_by = 'EMPRESA';

UPDATE expenses
SET paid_by = 'EXECUCAO'
WHERE paid_by = 'PARCEIRO';

ALTER TABLE IF EXISTS expenses
    ALTER COLUMN paid_by SET DEFAULT 'INNOVATIS';
