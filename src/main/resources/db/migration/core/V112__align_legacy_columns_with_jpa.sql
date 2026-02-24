-- Complements legacy/baselined schemas so Hibernate validate matches JPA models.
ALTER TABLE IF EXISTS budget_categories
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE IF EXISTS organizations
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;

ALTER TABLE IF EXISTS peoples
    ADD COLUMN IF NOT EXISTS updated_by BIGINT;
