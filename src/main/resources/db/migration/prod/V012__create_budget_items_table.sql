CREATE TABLE budget_items (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity INTEGER,
    months INTEGER,
    unit_cost NUMERIC(15,2),
    planned_amount NUMERIC(15,2) NOT NULL,
    executed_amount NUMERIC(15,2) NOT NULL DEFAULT 0,
    goal_id BIGINT,
    notes TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_budget_item_category_id
        FOREIGN KEY (category_id) REFERENCES budget_categories (id),
    CONSTRAINT fk_budget_item_goal_id
        FOREIGN KEY (goal_id) REFERENCES goals (id)
);

CREATE INDEX idx_budget_items_category_id_is_active ON budget_items (category_id, is_active);
CREATE INDEX idx_budget_items_goal_id ON budget_items (goal_id);

