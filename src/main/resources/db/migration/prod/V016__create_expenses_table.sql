CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    budget_item_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    income_id BIGINT NOT NULL,
    expense_date DATE NOT NULL,
    quantity INTEGER NOT NULL,
    amount NUMERIC(15,2) NOT NULL,
    person_id BIGINT,
    organization_id BIGINT,
    description VARCHAR(255),
    invoice_number VARCHAR(100),
    invoice_date DATE,
    document_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    CONSTRAINT fk_expense_budget_item_id
        FOREIGN KEY (budget_item_id) REFERENCES budget_items (id),
    CONSTRAINT fk_expense_category_id
        FOREIGN KEY (category_id) REFERENCES budget_categories (id),
    CONSTRAINT fk_expense_income_id
        FOREIGN KEY (income_id) REFERENCES incomes (id),
    CONSTRAINT fk_expense_person_id
        FOREIGN KEY (person_id) REFERENCES peoples (id),
    CONSTRAINT fk_expense_organization_id
        FOREIGN KEY (organization_id) REFERENCES organizations (id),
    CONSTRAINT fk_expense_document_id
        FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE INDEX idx_expenses_income_id_is_active ON expenses (income_id, is_active);
CREATE INDEX idx_expenses_budget_item_id ON expenses (budget_item_id);
CREATE INDEX idx_expenses_category_id ON expenses (category_id);

