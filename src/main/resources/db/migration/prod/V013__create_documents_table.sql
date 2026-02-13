CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type VARCHAR(50) NOT NULL,
    owner_id BIGINT NOT NULL,
    category VARCHAR(100),
    original_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100),
    size_bytes BIGINT NOT NULL,
    sha256 VARCHAR(64),
    bucket VARCHAR(255) NOT NULL,
    s3_key VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_documents_s3_key UNIQUE (s3_key)
);

CREATE INDEX idx_documents_owner ON documents (owner_type, owner_id);
CREATE INDEX idx_documents_status ON documents (status);
