ALTER TABLE IF EXISTS partners
    DROP CONSTRAINT IF EXISTS fk_partners_responsible_person;

DROP INDEX IF EXISTS idx_partners_responsible_person_id;

ALTER TABLE IF EXISTS partners
    DROP COLUMN IF EXISTS responsible_person_id;
