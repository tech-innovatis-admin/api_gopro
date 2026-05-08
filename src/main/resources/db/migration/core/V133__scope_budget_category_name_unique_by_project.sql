DO $$
DECLARE
    unique_constraint RECORD;
BEGIN
    FOR unique_constraint IN
        SELECT constraint_info.conname
        FROM pg_constraint constraint_info
        WHERE constraint_info.conrelid = to_regclass('budget_categories')
          AND constraint_info.contype = 'u'
          AND (
              SELECT array_agg(attribute_info.attname::text ORDER BY key_info.ordinality)
              FROM unnest(constraint_info.conkey) WITH ORDINALITY AS key_info(attnum, ordinality)
              JOIN pg_attribute attribute_info
                ON attribute_info.attrelid = constraint_info.conrelid
               AND attribute_info.attnum = key_info.attnum
          ) = ARRAY['name']
    LOOP
        EXECUTE format('ALTER TABLE budget_categories DROP CONSTRAINT %I', unique_constraint.conname);
    END LOOP;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_budget_categories_project_name
    ON budget_categories (project_id, name);
