CREATE TABLE budget_categories (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT,
    code VARCHAR(50) UNIQUE,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    CONSTRAINT fk_budget_category_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE INDEX idx_budget_categories_project_id_is_active ON budget_categories (project_id, is_active);

