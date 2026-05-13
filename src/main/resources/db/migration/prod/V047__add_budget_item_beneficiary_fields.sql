ALTER TABLE budget_items
    ADD COLUMN IF NOT EXISTS project_people_id bigint,
    ADD COLUMN IF NOT EXISTS project_company_id bigint,
    ADD COLUMN IF NOT EXISTS beneficiary_type varchar(20),
    ADD COLUMN IF NOT EXISTS contracted_amount numeric(15,2);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_budget_items_project_people_id'
    ) THEN
        ALTER TABLE budget_items
            ADD CONSTRAINT fk_budget_items_project_people_id
                FOREIGN KEY (project_people_id) REFERENCES project_people (id);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_budget_items_project_company_id'
    ) THEN
        ALTER TABLE budget_items
            ADD CONSTRAINT fk_budget_items_project_company_id
                FOREIGN KEY (project_company_id) REFERENCES project_company (id);
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_budget_items_single_beneficiary_ref'
    ) THEN
        ALTER TABLE budget_items
            ADD CONSTRAINT ck_budget_items_single_beneficiary_ref
                CHECK (NOT (project_people_id IS NOT NULL AND project_company_id IS NOT NULL));
    END IF;
END
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'ck_budget_items_beneficiary_type'
    ) THEN
        ALTER TABLE budget_items
            ADD CONSTRAINT ck_budget_items_beneficiary_type
                CHECK (
                    beneficiary_type IS NULL
                    OR beneficiary_type IN ('person', 'company')
                );
    END IF;
END
$$;

DO $$
DECLARE
    item_rec RECORD;
    resolved_project_people_id bigint;
BEGIN
    FOR item_rec IN
        SELECT bi.id AS budget_item_id,
               bc.project_id,
               bi.planned_amount
        FROM budget_items bi
        JOIN budget_categories bc ON bc.id = bi.category_id
        WHERE bi.is_active = true
    LOOP
        BEGIN
            SELECT pp.id
            INTO resolved_project_people_id
            FROM expenses e
            JOIN project_people pp
              ON pp.person_id = e.person_id
             AND pp.project_id = item_rec.project_id
            WHERE e.budget_item_id = item_rec.budget_item_id
              AND e.person_id IS NOT NULL
            ORDER BY e.id
            LIMIT 1;

            IF resolved_project_people_id IS NOT NULL THEN
                UPDATE budget_items
                SET project_people_id = resolved_project_people_id,
                    project_company_id = NULL,
                    beneficiary_type = 'person',
                    contracted_amount = COALESCE(contracted_amount, planned_amount)
                WHERE id = item_rec.budget_item_id
                  AND project_people_id IS NULL
                  AND project_company_id IS NULL;
            END IF;

            IF EXISTS (
                SELECT 1
                FROM expenses e
                WHERE e.budget_item_id = item_rec.budget_item_id
                  AND e.organization_id IS NOT NULL
            ) THEN
                RAISE NOTICE 'Budget item % possui expenses.organization_id e requer revisao manual de beneficiario.',
                    item_rec.budget_item_id;
            END IF;
        EXCEPTION
            WHEN OTHERS THEN
                RAISE WARNING 'Falha no backfill do budget_item_id=%: %',
                    item_rec.budget_item_id, SQLERRM;
                CONTINUE;
        END;
    END LOOP;
END
$$;
