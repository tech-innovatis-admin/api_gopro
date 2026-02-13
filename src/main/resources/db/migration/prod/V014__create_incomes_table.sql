CREATE TABLE incomes (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    numero INTEGER NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    received_at DATE NOT NULL,
    source VARCHAR(255),
    invoice_number VARCHAR(100),
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_income_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE INDEX idx_incomes_project_id_is_active ON incomes (project_id, is_active);

