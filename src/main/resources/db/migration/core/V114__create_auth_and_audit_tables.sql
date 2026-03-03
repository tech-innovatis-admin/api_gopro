CREATE TABLE IF NOT EXISTS users (
    id bigserial PRIMARY KEY,
    username varchar(100),
    email varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    full_name varchar(255) NOT NULL,
    role varchar(30) NOT NULL,
    status varchar(30) NOT NULL DEFAULT 'PENDING',
    last_login_at timestamp,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT ck_users_role CHECK (role IN ('SUPERADMIN', 'ADMIN', 'ANALISTA', 'ESTAGIARIO')),
    CONSTRAINT ck_users_status CHECK (status IN ('ACTIVE', 'DISABLED', 'PENDING'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_lower ON users (lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_username_lower ON users (lower(username)) WHERE username IS NOT NULL;

CREATE TABLE IF NOT EXISTS allowed_registrations (
    id bigserial PRIMARY KEY,
    email varchar(255) NOT NULL,
    role varchar(30) NOT NULL,
    invite_token_hash varchar(128) NOT NULL,
    invited_by_user_id bigint NOT NULL,
    invited_at timestamp NOT NULL DEFAULT now(),
    expires_at timestamp NOT NULL,
    used_at timestamp,
    status varchar(30) NOT NULL DEFAULT 'PENDING',
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_allowed_registrations_invited_by_user
        FOREIGN KEY (invited_by_user_id) REFERENCES users (id),
    CONSTRAINT ck_allowed_registrations_role CHECK (role IN ('SUPERADMIN', 'ADMIN', 'ANALISTA', 'ESTAGIARIO')),
    CONSTRAINT ck_allowed_registrations_status CHECK (status IN ('PENDING', 'USED', 'EXPIRED', 'CANCELLED'))
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_allowed_registrations_email_lower ON allowed_registrations (lower(email));
CREATE UNIQUE INDEX IF NOT EXISTS uk_allowed_registrations_token_hash ON allowed_registrations (invite_token_hash);
CREATE INDEX IF NOT EXISTS idx_allowed_registrations_status ON allowed_registrations (status);
CREATE INDEX IF NOT EXISTS idx_allowed_registrations_expires_at ON allowed_registrations (expires_at);

CREATE TABLE IF NOT EXISTS audit_log (
    id bigserial PRIMARY KEY,
    actor_user_id bigint,
    action varchar(120) NOT NULL,
    entity_type varchar(120) NOT NULL,
    entity_id varchar(120),
    before_json jsonb,
    after_json jsonb,
    ip varchar(64),
    user_agent varchar(512),
    created_at timestamp NOT NULL DEFAULT now(),
    CONSTRAINT fk_audit_log_actor_user
        FOREIGN KEY (actor_user_id) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_audit_log_created_at ON audit_log (created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_actor_user_id ON audit_log (actor_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log (action);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log (entity_type, entity_id);
