ALTER TABLE IF EXISTS companies
    ADD COLUMN IF NOT EXISTS responsible_person_id BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_companies_responsible_person'
    ) THEN
        ALTER TABLE companies
            ADD CONSTRAINT fk_companies_responsible_person
                FOREIGN KEY (responsible_person_id) REFERENCES peoples (id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_companies_responsible_person_id
    ON companies (responsible_person_id);
