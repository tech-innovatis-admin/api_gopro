CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    project_status VARCHAR(50) NOT NULL,
    area_segmento VARCHAR(255),
    object TEXT NOT NULL,
    primary_partner_id BIGINT NOT NULL,
    secundary_partner_id BIGINT,
    primary_client_id BIGINT NOT NULL,
    secundary_client_id BIGINT,
    cordinator_id BIGINT,
    project_gov_if VARCHAR(50),
    project_type VARCHAR(50),
    contract_value NUMERIC(15,2),
    start_date DATE,
    end_date DATE,
    opening_date DATE,
    closing_date DATE,
    city VARCHAR(255),
    state VARCHAR(255),
    execution_location VARCHAR(400),
    total_received NUMERIC(35,2) NOT NULL DEFAULT 0,
    total_expenses NUMERIC(35,2) NOT NULL DEFAULT 0,
    saldo NUMERIC(35,2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_project_primary_partner
        FOREIGN KEY (primary_partner_id) REFERENCES partners (id),
    CONSTRAINT fk_project_secundary_partner
        FOREIGN KEY (secundary_partner_id) REFERENCES partners (id),
    CONSTRAINT fk_project_primary_client
        FOREIGN KEY (primary_client_id) REFERENCES public_agencies (id),
    CONSTRAINT fk_project_secundary_client
        FOREIGN KEY (secundary_client_id) REFERENCES secretariats (id),
    CONSTRAINT fk_project_cordinator_id
        FOREIGN KEY (cordinator_id) REFERENCES peoples (id)
);

CREATE INDEX idx_projects_is_active ON projects (is_active);
CREATE INDEX idx_projects_status ON projects (project_status);
CREATE INDEX idx_projects_primary_partner_id ON projects (primary_partner_id);
CREATE INDEX idx_projects_primary_client_id ON projects (primary_client_id);
CREATE INDEX idx_projects_active_coalesced_date
    ON projects ((COALESCE(start_date, opening_date)))
    WHERE is_active = TRUE;

