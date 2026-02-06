CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type varchar(50) NOT NULL,
    owner_id bigint NOT NULL,
    category varchar(100),
    original_name varchar(255) NOT NULL,
    content_type varchar(100),
    size_bytes bigint NOT NULL,
    sha256 varchar(64),
    bucket varchar(255) NOT NULL,
    s3_key varchar(500) NOT NULL,
    status varchar(20) NOT NULL,
    created_at timestamp,
    updated_at timestamp,
    created_by bigint,
    deleted_at timestamp
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_documents_s3_key ON documents (s3_key);
CREATE INDEX IF NOT EXISTS idx_documents_owner ON documents (owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents (status);
