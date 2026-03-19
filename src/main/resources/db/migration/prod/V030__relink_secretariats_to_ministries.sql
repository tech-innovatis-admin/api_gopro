-- Garante vinculo consistente entre ministerios e secretarias para selecao hierarquica no front.
-- Regra de relink: ultima parte da sigla da secretaria deve corresponder a sigla do ministerio.
-- Exemplo: "SE/MAPA" -> ministerio "MAPA".

WITH normalized_ministry AS (
    SELECT
        pa.id,
        upper(regexp_replace(trim(pa.sigla), '\s+', '', 'g')) AS sigla
    FROM public_agencies pa
    WHERE pa.public_agency_type = 'MINISTERIO'
      AND pa.sigla IS NOT NULL
), secretary_with_sigla AS (
    SELECT
        s.id AS secretary_id,
        s.public_agency_id AS current_public_agency_id,
        upper(regexp_replace(trim(s.sigla), '\s+', '', 'g')) AS secretary_sigla
    FROM secretariats s
    WHERE s.sigla IS NOT NULL
), secretary_target_ministry AS (
    SELECT
        sws.secretary_id,
        sws.current_public_agency_id,
        nm.id AS target_public_agency_id
    FROM secretary_with_sigla sws
    JOIN normalized_ministry nm
      ON nm.sigla = split_part(
            sws.secretary_sigla,
            '/',
            array_length(string_to_array(sws.secretary_sigla, '/'), 1)
         )
)
UPDATE secretariats s
SET
    public_agency_id = stm.target_public_agency_id,
    is_active = TRUE,
    updated_at = NOW(),
    updated_by = COALESCE(s.updated_by, 1)
FROM secretary_target_ministry stm
WHERE s.id = stm.secretary_id
  AND s.public_agency_id IS DISTINCT FROM stm.target_public_agency_id;

-- Mantem ministerios e secretarias deste relacionamento prontos para uso no cadastro de contratos.
UPDATE public_agencies pa
SET
    is_active = TRUE,
    is_client = TRUE,
    updated_at = NOW(),
    updated_by = COALESCE(pa.updated_by, 1)
WHERE pa.public_agency_type = 'MINISTERIO'
  AND EXISTS (
      SELECT 1
      FROM secretariats s
      WHERE s.public_agency_id = pa.id
  );

UPDATE secretariats s
SET
    is_active = TRUE,
    is_client = TRUE,
    updated_at = NOW(),
    updated_by = COALESCE(s.updated_by, 1)
WHERE EXISTS (
    SELECT 1
    FROM public_agencies pa
    WHERE pa.id = s.public_agency_id
      AND pa.public_agency_type = 'MINISTERIO'
);

CREATE INDEX IF NOT EXISTS idx_secretariats_public_agency_active_client
    ON secretariats (public_agency_id, is_active, is_client);
