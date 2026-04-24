ALTER TABLE IF EXISTS partners
    ADD COLUMN IF NOT EXISTS responsible_person_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_partners_responsible_person'
    ) THEN
        ALTER TABLE partners
            ADD CONSTRAINT fk_partners_responsible_person
                FOREIGN KEY (responsible_person_id) REFERENCES peoples (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_partners_responsible_person_id
    ON partners (responsible_person_id);
