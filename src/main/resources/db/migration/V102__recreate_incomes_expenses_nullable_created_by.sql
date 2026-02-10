-- Recria as tabelas de incomes e expenses, preservando dados quando existirem,
-- e removendo a obrigatoriedade de created_by (nullable).

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'expenses'
    ) THEN
        DROP TABLE IF EXISTS expenses_backup_v102;
        EXECUTE 'CREATE TABLE expenses_backup_v102 AS TABLE expenses';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'incomes'
    ) THEN
        DROP TABLE IF EXISTS incomes_backup_v102;
        EXECUTE 'CREATE TABLE incomes_backup_v102 AS TABLE incomes';
    END IF;
END $$;

DROP TABLE IF EXISTS expenses;
DROP TABLE IF EXISTS incomes;

CREATE TABLE incomes (
    id bigserial PRIMARY KEY,
    project_id bigint NOT NULL,
    numero integer NOT NULL,
    amount numeric(15,2) NOT NULL,
    received_at date NOT NULL,
    source varchar(255),
    invoice_number varchar(100),
    notes text,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_income_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE expenses (
    id bigserial PRIMARY KEY,
    budget_item_id bigint NOT NULL,
    category_id bigint NOT NULL,
    income_id bigint NOT NULL,
    expense_date date NOT NULL,
    quantity integer NOT NULL,
    amount numeric(15,2) NOT NULL,
    person_id bigint,
    organization_id bigint,
    description varchar(255),
    invoice_number varchar(100),
    invoice_date date,
    document_id uuid,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
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

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'incomes_backup_v102'
    ) THEN
        INSERT INTO incomes (
            id,
            project_id,
            numero,
            amount,
            received_at,
            source,
            invoice_number,
            notes,
            is_active,
            created_at,
            updated_at,
            created_by,
            updated_by
        )
        SELECT
            id,
            project_id,
            numero,
            amount,
            received_at,
            source,
            invoice_number,
            notes,
            is_active,
            created_at,
            updated_at,
            created_by,
            updated_by
        FROM incomes_backup_v102;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'expenses_backup_v102'
    ) THEN
        INSERT INTO expenses (
            id,
            budget_item_id,
            category_id,
            income_id,
            expense_date,
            quantity,
            amount,
            person_id,
            organization_id,
            description,
            invoice_number,
            invoice_date,
            document_id,
            is_active,
            created_at,
            updated_at,
            created_by,
            updated_by
        )
        SELECT
            e.id,
            e.budget_item_id,
            e.category_id,
            e.income_id,
            e.expense_date,
            e.quantity,
            e.amount,
            e.person_id,
            e.organization_id,
            e.description,
            e.invoice_number,
            e.invoice_date,
            e.document_id,
            e.is_active,
            e.created_at,
            e.updated_at,
            e.created_by,
            e.updated_by
        FROM expenses_backup_v102 e
        WHERE EXISTS (
            SELECT 1
            FROM incomes i
            WHERE i.id = e.income_id
        );
    END IF;
END $$;

SELECT setval(
    pg_get_serial_sequence('incomes', 'id'),
    COALESCE((SELECT MAX(id) FROM incomes), 0) + 1,
    false
);

SELECT setval(
    pg_get_serial_sequence('expenses', 'id'),
    COALESCE((SELECT MAX(id) FROM expenses), 0) + 1,
    false
);

DROP TABLE IF EXISTS expenses_backup_v102;
DROP TABLE IF EXISTS incomes_backup_v102;
