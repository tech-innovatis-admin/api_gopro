CREATE TABLE public_agencies (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    sigla VARCHAR(20),
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14) UNIQUE,
    is_client BOOLEAN NOT NULL,
    public_agency_type VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    address VARCHAR(400),
    contact_person VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

CREATE INDEX idx_public_agencies_is_active ON public_agencies (is_active);
