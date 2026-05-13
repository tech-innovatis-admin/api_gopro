CREATE OR REPLACE VIEW vw_beneficiary_budget_summary AS
SELECT
    bi.id AS budget_item_id,
    bc.project_id,
    bi.category_id,
    bi.description AS budget_item_description,
    bi.beneficiary_type,
    bi.project_people_id,
    bi.project_company_id,
    CASE
        WHEN bi.beneficiary_type = 'person' THEN p.full_name
        WHEN bi.beneficiary_type = 'company' THEN c.name
        ELSE NULL
    END AS beneficiary_name,
    CASE
        WHEN bi.beneficiary_type = 'person' THEN CAST(pp.role AS varchar)
        WHEN bi.beneficiary_type = 'company' THEN pc.service_type
        ELSE NULL
    END AS beneficiary_role,
    bi.contracted_amount,
    bi.planned_amount,
    COALESCE(SUM(e.amount) FILTER (WHERE e.is_active = true), 0::numeric) AS total_received,
    bi.contracted_amount - COALESCE(SUM(e.amount) FILTER (WHERE e.is_active = true), 0::numeric) AS balance,
    ROUND(
        (
            COALESCE(SUM(e.amount) FILTER (WHERE e.is_active = true), 0::numeric)
            / NULLIF(bi.contracted_amount, 0::numeric)
        ) * 100::numeric,
        2
    ) AS percent_executed,
    (
        COALESCE(SUM(e.amount) FILTER (WHERE e.is_active = true), 0::numeric)
        > COALESCE(bi.contracted_amount, 0::numeric)
    ) AS is_over_budget
FROM budget_items bi
JOIN budget_categories bc ON bc.id = bi.category_id
LEFT JOIN project_people pp ON pp.id = bi.project_people_id
LEFT JOIN peoples p ON p.id = pp.person_id
LEFT JOIN project_company pc ON pc.id = bi.project_company_id
LEFT JOIN companies c ON c.id = pc.company_id
LEFT JOIN expenses e ON e.budget_item_id = bi.id
GROUP BY
    bi.id,
    bc.project_id,
    bi.category_id,
    bi.description,
    bi.beneficiary_type,
    bi.project_people_id,
    bi.project_company_id,
    p.full_name,
    c.name,
    pp.role,
    pc.service_type,
    bi.contracted_amount,
    bi.planned_amount;
