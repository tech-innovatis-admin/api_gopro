ALTER TABLE IF EXISTS goals
    ADD COLUMN IF NOT EXISTS has_financial_value BOOLEAN;

ALTER TABLE IF EXISTS goals
    ADD COLUMN IF NOT EXISTS financial_amount NUMERIC(15, 2);

UPDATE goals
SET has_financial_value = CASE
    WHEN financial_amount IS NOT NULL AND financial_amount > 0 THEN TRUE
    ELSE FALSE
END
WHERE has_financial_value IS NULL;

UPDATE goals
SET financial_amount = NULL
WHERE has_financial_value = FALSE;

ALTER TABLE IF EXISTS goals
    ALTER COLUMN has_financial_value SET DEFAULT FALSE;

UPDATE goals
SET has_financial_value = FALSE
WHERE has_financial_value IS NULL;

ALTER TABLE IF EXISTS goals
    ALTER COLUMN has_financial_value SET NOT NULL;
