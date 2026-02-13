CREATE TABLE secretariats (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE,
    sigla VARCHAR(20),
    public_agency_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14),
    is_client BOOLEAN NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    address VARCHAR(400),
    contact_person VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_secretary_public_agency
        FOREIGN KEY (public_agency_id) REFERENCES public_agencies (id)
);

CREATE INDEX idx_secretariats_public_agency_id ON secretariats (public_agency_id);
CREATE INDEX idx_secretariats_is_active ON secretariats (is_active);
