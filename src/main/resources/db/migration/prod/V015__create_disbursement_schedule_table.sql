CREATE TABLE disbursement_schedule (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    numero INTEGER NOT NULL,
    expected_month DATE NOT NULL,
    expected_amount NUMERIC(15,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_disbursement_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE INDEX idx_disbursement_schedule_project_id_is_active
    ON disbursement_schedule (project_id, is_active);

