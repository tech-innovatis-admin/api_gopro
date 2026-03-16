ALTER TABLE IF EXISTS projects
    ADD COLUMN IF NOT EXISTS executed_by_innovatis BOOLEAN;

UPDATE projects
SET executed_by_innovatis = FALSE
WHERE executed_by_innovatis IS NULL;

ALTER TABLE IF EXISTS projects
    ALTER COLUMN executed_by_innovatis SET DEFAULT FALSE;

ALTER TABLE IF EXISTS projects
    ALTER COLUMN executed_by_innovatis SET NOT NULL;
