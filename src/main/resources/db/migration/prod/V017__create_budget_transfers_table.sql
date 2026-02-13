CREATE TABLE budget_transfers (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    from_item_id BIGINT NOT NULL,
    to_item_id BIGINT NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    transfer_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    reason TEXT,
    document_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by BIGINT,
    updated_at TIMESTAMP,
    updated_by BIGINT,
    approved_at TIMESTAMP,
    approved_by BIGINT,
    CONSTRAINT fk_budget_transfer_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_budget_transfer_from_item_id
        FOREIGN KEY (from_item_id) REFERENCES budget_items (id),
    CONSTRAINT fk_budget_transfer_to_item_id
        FOREIGN KEY (to_item_id) REFERENCES budget_items (id),
    CONSTRAINT fk_budget_transfer_document_id
        FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE INDEX idx_budget_transfers_project_id_is_active ON budget_transfers (project_id, is_active);
CREATE INDEX idx_budget_transfers_from_item_id ON budget_transfers (from_item_id);
CREATE INDEX idx_budget_transfers_to_item_id ON budget_transfers (to_item_id);

