CREATE SEQUENCE project_company_contract_number_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE project_company (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    contract_number VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    start_date DATE,
    end_date DATE,
    status SMALLINT,
    total_value NUMERIC(15,2),
    notes TEXT,
    is_incubated BOOLEAN,
    service_type VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_project_company_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_company_company_id
        FOREIGN KEY (company_id) REFERENCES companies (id)
);

CREATE INDEX idx_project_company_project_id_is_active ON project_company (project_id, is_active);
CREATE INDEX idx_project_company_company_id_is_active ON project_company (company_id, is_active);
