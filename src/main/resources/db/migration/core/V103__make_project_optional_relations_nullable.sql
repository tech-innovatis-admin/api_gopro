ALTER TABLE projects
    ALTER COLUMN secundary_partner_id DROP NOT NULL,
    ALTER COLUMN secundary_client_id DROP NOT NULL,
    ALTER COLUMN cordinator_id DROP NOT NULL;

