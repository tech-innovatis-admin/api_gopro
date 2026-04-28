ALTER TABLE users
    ADD COLUMN IF NOT EXISTS auth_tokens_invalid_before timestamp;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id bigserial PRIMARY KEY,
    user_id bigint NOT NULL,
    token_hash varchar(128) NOT NULL,
    expires_at timestamp NOT NULL,
    used_at timestamp,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_password_reset_tokens_token_hash
    ON password_reset_tokens (token_hash);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);

CREATE INDEX IF NOT EXISTS idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);
