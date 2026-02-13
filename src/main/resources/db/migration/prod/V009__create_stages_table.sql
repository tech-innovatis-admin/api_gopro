CREATE TABLE stages (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
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
    CONSTRAINT fk_stage_goal_id
        FOREIGN KEY (goal_id) REFERENCES goals (id)
);

CREATE INDEX idx_stages_goal_id_is_active ON stages (goal_id, is_active);

