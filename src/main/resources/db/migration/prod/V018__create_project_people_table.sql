CREATE TABLE project_people (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,
    role VARCHAR(255),
    workload_hours NUMERIC(5,2),
    institutional_link VARCHAR(255),
    contract_type VARCHAR(50),
    start_date DATE,
    end_date DATE,
    status VARCHAR(50),
    base_amount NUMERIC(15,2),
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_project_people_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_people_person_id
        FOREIGN KEY (person_id) REFERENCES peoples (id)
);

CREATE INDEX idx_project_people_project_id_is_active ON project_people (project_id, is_active);
CREATE INDEX idx_project_people_person_id_is_active ON project_people (person_id, is_active);
CREATE UNIQUE INDEX uq_project_people_active_project_person
    ON project_people (project_id, person_id)
    WHERE is_active = TRUE;

