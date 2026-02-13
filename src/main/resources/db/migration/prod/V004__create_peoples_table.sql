CREATE TABLE peoples (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    cpf VARCHAR(14) UNIQUE,
    email VARCHAR(255),
    phone VARCHAR(50),
    avatar_url TEXT,
    birth_date DATE,
    address VARCHAR(500),
    zip_code VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(50),
    notes VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    update_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_peoples_is_active ON peoples (is_active);
