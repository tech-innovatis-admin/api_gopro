ALTER TABLE audit_log
    ADD COLUMN IF NOT EXISTS audit_id varchar(36),
    ADD COLUMN IF NOT EXISTS event_at timestamp with time zone,
    ADD COLUMN IF NOT EXISTS tipo_auditoria varchar(30),
    ADD COLUMN IF NOT EXISTS modulo varchar(120),
    ADD COLUMN IF NOT EXISTS feature varchar(160),
    ADD COLUMN IF NOT EXISTS entidade_principal varchar(120),
    ADD COLUMN IF NOT EXISTS aba varchar(120),
    ADD COLUMN IF NOT EXISTS subsecao varchar(120),
    ADD COLUMN IF NOT EXISTS resumo varchar(500),
    ADD COLUMN IF NOT EXISTS descricao text,
    ADD COLUMN IF NOT EXISTS resultado varchar(20),
    ADD COLUMN IF NOT EXISTS correlacao_id varchar(120),
    ADD COLUMN IF NOT EXISTS alteracoes_json jsonb,
    ADD COLUMN IF NOT EXISTS detalhes_tecnicos_json jsonb;

UPDATE audit_log
SET event_at = created_at
WHERE event_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_audit_log_tipo_auditoria ON audit_log (tipo_auditoria);
CREATE INDEX IF NOT EXISTS idx_audit_log_correlacao_id ON audit_log (correlacao_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_event_at ON audit_log (event_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_contract_timeline ON audit_log (tipo_auditoria, entity_id, event_at DESC);
CREATE UNIQUE INDEX IF NOT EXISTS uk_audit_log_audit_id ON audit_log (audit_id) WHERE audit_id IS NOT NULL;
