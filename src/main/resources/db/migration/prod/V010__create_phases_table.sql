CREATE TABLE phases (
    id BIGSERIAL PRIMARY KEY,
    stage_id BIGINT NOT NULL,
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
    CONSTRAINT fk_phase_stage_id
        FOREIGN KEY (stage_id) REFERENCES stages (id)
);

CREATE INDEX idx_phases_stage_id_is_active ON phases (stage_id, is_active);
