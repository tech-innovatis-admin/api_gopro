-- Guarantees CPF is optional for people registrations.
ALTER TABLE IF EXISTS peoples
    ALTER COLUMN cpf DROP NOT NULL;

UPDATE peoples
SET cpf = NULL
WHERE cpf IS NOT NULL
  AND btrim(cpf) = '';
