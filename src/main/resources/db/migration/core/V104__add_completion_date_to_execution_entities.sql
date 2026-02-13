ALTER TABLE goals
    ADD COLUMN IF NOT EXISTS data_conclusao date;

ALTER TABLE stages
    ADD COLUMN IF NOT EXISTS data_conclusao date;

ALTER TABLE phases
    ADD COLUMN IF NOT EXISTS data_conclusao date;
