CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    numero INTEGER NOT NULL,
    titulo VARCHAR(255) NOT NULL,
    descricao TEXT,
    data_inicio DATE,
    data_fim DATE,
    data_conclusao DATE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_goal_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE INDEX idx_goals_project_id_is_active ON goals (project_id, is_active);

