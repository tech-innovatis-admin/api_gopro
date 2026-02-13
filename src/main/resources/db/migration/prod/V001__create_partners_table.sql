CREATE TABLE partners (
    id BIGSERIAL PRIMARY KEY,
    acronym VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    trade_name VARCHAR(255) NOT NULL,
    partners_type VARCHAR(50) NOT NULL,
    cnpj VARCHAR(14) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    address VARCHAR(400) NOT NULL,
    site VARCHAR(300),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_partners_is_active ON partners (is_active);
CREATE INDEX idx_partners_cnpj ON partners (cnpj);
