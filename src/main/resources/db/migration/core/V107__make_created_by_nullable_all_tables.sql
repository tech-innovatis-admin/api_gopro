-- Torna created_by opcional em todas as tabelas.
-- Necessario para ambiente sem autenticacao (v1), evitando erros por valor nulo.
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT table_schema, table_name
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND column_name = 'created_by'
          AND is_nullable = 'NO'
    LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I ALTER COLUMN created_by DROP NOT NULL',
            rec.table_schema,
            rec.table_name
        );
    END LOOP;
END $$;

