INSERT INTO partners (id, acronym, name, trade_name, partners_type, cnpj, email, phone, address, site, city, state, is_active, created_by, created_at)
VALUES
  (1, 'IFMA', 'Instituto Federal do Maranhao', 'IFMA', 'IF', '12345678000190', 'contato@ifma.edu.br', '9830000000', 'Av. do IFMA, 1000', 'https://ifma.edu.br', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (2, 'FUND-GOPRO', 'Fundacao GoPro', 'Fundacao GoPro', 'FUNDACAO', '23456789000100', 'fundacao@gopro.org', '9820000000', 'Rua das Fundacoes, 200', 'https://fundacaogopro.org', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public_agencies (id, code, sigla, name, cnpj, is_client, public_agency_type, email, phone, address, contact_person, city, state, is_active, created_by, created_at)
VALUES
  (1, 'PREF-SLZ', 'PMSL', 'Prefeitura Municipal de Sao Luis', '34567890000110', true, 'PREFEITURA', 'prefeitura@saoluis.ma.gov.br', '9831111111', 'Av. Pedro II, 100', 'Joao da Silva', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (2, 'GOV-MA', 'GOVMA', 'Governo do Estado do Maranhao', '45678900000120', true, 'GOVERNO_ESTADUAL', 'governo@ma.gov.br', '9832222222', 'Palacio dos Leoes, S/N', 'Maria Souza', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (3, 'MEC', 'MEC', 'Ministerio da Educacao', '56789000000130', true, 'MINISTERIO', 'contato@mec.gov.br', '6133333333', 'Esplanada dos Ministerios, Bloco L', 'Carlos Pereira', 'Brasilia', 'DF', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO secretariats (id, code, sigla, public_agency_id, name, cnpj, is_client, email, phone, address, contact_person, is_active, created_by, created_at)
VALUES
  (1, 'SEMUS-SLZ', 'SEMUS', 1, 'Secretaria Municipal de Saude', '67890000000140', true, 'semus@saoluis.ma.gov.br', '9834444444', 'Rua da Saude, 50', 'Ana Lima', true, 1, CURRENT_TIMESTAMP),
  (2, 'SEDUC-MA', 'SEDUC', 2, 'Secretaria de Estado da Educacao', '78900000000150', true, 'seduc@ma.gov.br', '9835555555', 'Av. Educacao, 500', 'Bruno Costa', true, 1, CURRENT_TIMESTAMP),
  (3, 'SETEC-MEC', 'SETEC', 3, 'Secretaria de Educacao Profissional e Tecnologica', '89000000000160', true, 'setec@mec.gov.br', '6136666666', 'Esplanada, Bloco L', 'Paula Ramos', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO peoples (id, full_name, cpf, email, phone, birth_date, address, zip_code, city, state, is_active, created_by, created_at)
VALUES
  (1, 'Marcos Andrade', '12345678901', 'marcos.andrade@gopro.org', '9899999999', '1985-04-20', 'Rua Central, 10', '65000000', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (2, 'Fernanda Oliveira', '23456789012', 'fernanda.oliveira@gopro.org', '9898888888', '1990-09-15', 'Av. Norte, 200', '65000001', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organizations (id, name, trade_name, cnpj, type, email, phone, address, contact_person, zip_code, city, state, notes, is_active, created_by, created_at)
VALUES
  (1, 'Fundacao de Apoio ao Desenvolvimento', 'FAD', '34567000000199', 0, 'contato@fad.org', '9837777777', 'Av. das Fundacoes, 100', 'Juliana Prado', '65010000', 'Sao Luis', 'MA', 'Fundacao de apoio tecnico', true, 1, CURRENT_TIMESTAMP),
  (2, 'Instituto de Pesquisa Regional', 'IPR', '45678000000188', 2, 'contato@ipr.org', '9838888888', 'Rua da Pesquisa, 250', 'Ricardo Luz', '65010001', 'Sao Luis', 'MA', 'Instituto parceiro', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO companies (id, name, trade_name, cnpj, email, phone, address, city, state, is_active, created_by, created_at)
VALUES
  (1, 'Tech Maranhense LTDA', 'TechMA', '55667788000110', 'contato@techma.com', '9831234567', 'Av. Tec, 400', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (2, 'Inova Solutions LTDA', 'Inova', '66778899000120', 'contato@inova.com', '9837654321', 'Rua Inovacao, 800', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, name, code, project_status, area_segmento, "object", primary_partner_id, secundary_partner_id, primary_client_id, secundary_client_id, cordinator_id, project_gov_if, project_type, contract_value, start_date, end_date, opening_date, closing_date, city, state, execution_location, total_received, total_expenses, saldo, is_active, created_by, created_at)
VALUES
  (1, 'Modernizacao de Unidades Basicas de Saude', 'PROJ-SAUDE-001', 'EXECUCAO', 'Saude', 'Modernizar UBSs com equipamentos e sistemas', 1, 2, 1, 1, 1, 'GOV', 'PROJETO', 2500000.00, '2025-01-10', '2026-12-31', '2025-02-01', NULL, 'Sao Luis', 'MA', 'Sao Luis - MA', 500000.00, 120000.00, 380000.00, true, 1, CURRENT_TIMESTAMP),
  (2, 'Capacitacao Tecnologica em Escolas', 'PROJ-EDU-002', 'PLANEJAMENTO', 'Educacao', 'Formacao e laboratorios de tecnologia', 1, 2, 2, 2, 2, 'GOV', 'PROJETO', 1500000.00, '2025-03-01', '2027-03-01', '2025-04-01', NULL, 'Sao Luis', 'MA', 'Sao Luis - MA', 0.00, 0.00, 0.00, true, 1, CURRENT_TIMESTAMP),
  (3, 'Programa Nacional de Educacao Profissional', 'PROD-TEC-003', 'EXECUCAO', 'Educacao Profissional', 'Expansao de cursos tecnicos e laboratorios', 1, 2, 3, 3, 1, 'IF', 'PRODUTO', 5000000.00, '2024-08-01', '2026-08-01', '2024-09-01', NULL, 'Brasilia', 'DF', 'Brasilia - DF', 1200000.00, 300000.00, 900000.00, true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO goals (id, project_id, numero, titulo, descricao, data_inicio, data_fim, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 'Reestruturar UBSs', 'Adequacao fisica e tecnologica das UBSs', '2025-02-01', '2026-06-30', true, 1, CURRENT_TIMESTAMP),
  (2, 3, 1, 'Expandir oferta de cursos', 'Criar novos cursos tecnicos e laboratorios', '2024-09-01', '2026-06-30', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO stages (id, goal_id, numero, titulo, descricao, data_inicio, data_fim, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 'Diagnostico das UBSs', 'Levantamento de necessidades e infraestrutura', '2025-02-01', '2025-05-31', true, 1, CURRENT_TIMESTAMP),
  (2, 1, 2, 'Compra de equipamentos', 'Processos de compra e instalacao', '2025-06-01', '2026-03-31', true, 1, CURRENT_TIMESTAMP),
  (3, 2, 1, 'Planejamento curricular', 'Definicao de cursos e ementas', '2024-09-01', '2025-03-31', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO phases (id, stage_id, numero, titulo, descricao, data_inicio, data_fim, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 'Levantamento in loco', 'Visitas tecnicas e entrevistas', '2025-02-01', '2025-03-15', true, 1, CURRENT_TIMESTAMP),
  (2, 2, 1, 'Licitacao', 'Preparacao e publicacao do edital', '2025-06-01', '2025-09-30', true, 1, CURRENT_TIMESTAMP),
  (3, 3, 1, 'Definicao de laboratorios', 'Especificacoes tecnicas e layout', '2024-10-01', '2025-02-15', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO budget_categories (id, project_id, code, name, description, is_active, created_by, created_at)
VALUES
  (1, 1, 'SAUDE-EQP', 'Equipamentos de Saude', 'Compra de equipamentos para UBS', true, 1, CURRENT_TIMESTAMP),
  (2, 1, 'SAUDE-TI', 'Tecnologia da Informacao', 'Sistemas e infraestrutura de TI', true, 1, CURRENT_TIMESTAMP),
  (3, 3, 'EDU-LAB', 'Laboratorios Educacionais', 'Montagem de laboratorios tecnicos', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO budget_items (id, category_id, description, quantity, months, unit_cost, planned_amount, executed_amount, goal_id, notes, is_active, created_by, created_at)
VALUES
  (1, 1, 'Ultrassom portatil', 5, 1, 80000.00, 400000.00, 120000.00, 1, 'Compra para UBSs de maior demanda', true, 1, CURRENT_TIMESTAMP),
  (2, 2, 'Servidor de dados', 2, 1, 50000.00, 100000.00, 0.00, 1, 'Infraestrutura central', true, 1, CURRENT_TIMESTAMP),
  (3, 3, 'Kits de robotica', 20, 6, 3500.00, 70000.00, 0.00, 2, 'Laboratorios de tecnologia', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO incomes (id, project_id, numero, amount, received_at, source, invoice_number, notes, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 300000.00, '2025-02-15', 'Prefeitura de Sao Luis', 'NF-2025-001', 'Primeira parcela do convenio', true, 1, CURRENT_TIMESTAMP),
  (2, 1, 2, 200000.00, '2025-06-20', 'Prefeitura de Sao Luis', 'NF-2025-002', 'Segunda parcela do convenio', true, 1, CURRENT_TIMESTAMP),
  (3, 3, 1, 1200000.00, '2024-10-10', 'Ministerio da Educacao', 'NF-2024-010', 'Repasse federal', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO disbursement_schedule (id, project_id, numero, expected_month, expected_amount, status, notes, is_active, created_by, created_at)
VALUES
  (1, 1, 1, '2025-02-28', 300000.00, 'RECEBIDO', 'Primeira parcela recebida', true, 1, CURRENT_TIMESTAMP),
  (2, 1, 2, '2025-06-30', 200000.00, 'RECEBIDO', 'Segunda parcela recebida', true, 1, CURRENT_TIMESTAMP),
  (3, 3, 1, '2024-10-31', 1200000.00, 'RECEBIDO', 'Repasse MEC', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, owner_type, owner_id, category, original_name, content_type, size_bytes, sha256, bucket, s3_key, status, is_active, created_by, created_at)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'PROJECT', 1, 'contrato', 'contrato_projeto_1.pdf', 'application/pdf', 204800, NULL, 'local-bucket', 'documents/PROJECT/1/contrato_projeto_1.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP),
  ('22222222-2222-2222-2222-222222222222', 'INCOME', 1, 'nota_fiscal', 'nf_parcela_1.pdf', 'application/pdf', 102400, NULL, 'local-bucket', 'documents/INCOME/1/nf_parcela_1.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP),
  ('33333333-3333-3333-3333-333333333333', 'EXPENSE', 1, 'recibo', 'recibo_ultrassom.pdf', 'application/pdf', 51200, NULL, 'local-bucket', 'documents/EXPENSE/1/recibo_ultrassom.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP),
  ('44444444-4444-4444-4444-444444444444', 'COMPANY', 1, 'contrato', 'contrato_empresa_1.pdf', 'application/pdf', 153600, NULL, 'local-bucket', 'documents/COMPANY/1/contrato_empresa_1.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO expenses (id, budget_item_id, category_id, income_id, expense_date, quantity, amount, person_id, organization_id, description, invoice_number, invoice_date, document_id, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 1, '2025-03-10', 1, 120000.00, 1, NULL, 'Compra de ultrassom portatil', 'NF-EXP-001', '2025-03-08', '33333333-3333-3333-3333-333333333333', true, 1, CURRENT_TIMESTAMP),
  (2, 2, 2, 2, '2025-07-05', 1, 50000.00, NULL, 1, 'Aquisicao de servidor', 'NF-EXP-002', '2025-07-01', NULL, true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO budget_transfers (id, project_id, from_item_id, to_item_id, amount, transfer_date, status, reason, document_id, is_active, created_by, approved_at, approved_by, created_at)
VALUES
  (1, 1, 2, 1, 20000.00, '2025-08-15', 'APROVADO', 'Reforco para equipamentos prioritarios', NULL, true, 1, '2025-08-20 10:00:00', 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_people (id, project_id, person_id, role, workload_hours, institutional_link, contract_type, start_date, end_date, status, base_amount, notes, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 'DIRETOR', 20.00, 'IFMA', 'CLT', '2025-02-01', NULL, 'ATIVO', 12000.00, 'Coordenacao geral', true, 1, CURRENT_TIMESTAMP),
  (2, 2, 2, 'BOLSISTA', 10.00, 'IFMA', 'BOLSA', '2025-04-01', NULL, 'ATIVO', 3000.00, 'Apoio pedagogico', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_company (id, project_id, company_id, contract_number, description, start_date, end_date, status, total_value, notes, is_incubated, service_type, is_active, created_by, created_at)
VALUES
  (1, 1, 1, 'CT-2025-001', 'Contrato para fornecimento de equipamentos', '2025-02-15', '2026-02-14', 1, 400000.00, 'Entrega em 3 lotes', false, 'Fornecimento de equipamentos', true, 1, CURRENT_TIMESTAMP),
  (2, 3, 2, 'CT-2024-010', 'Contrato de desenvolvimento de laboratorios', '2024-10-01', '2026-09-30', 1, 900000.00, 'Treinamento incluso', true, 'Servicos de tecnologia', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('partners', 'id'), COALESCE((SELECT MAX(id) FROM partners), 1));
SELECT setval(pg_get_serial_sequence('public_agencies', 'id'), COALESCE((SELECT MAX(id) FROM public_agencies), 1));
SELECT setval(pg_get_serial_sequence('secretariats', 'id'), COALESCE((SELECT MAX(id) FROM secretariats), 1));
SELECT setval(pg_get_serial_sequence('peoples', 'id'), COALESCE((SELECT MAX(id) FROM peoples), 1));
SELECT setval(pg_get_serial_sequence('organizations', 'id'), COALESCE((SELECT MAX(id) FROM organizations), 1));
SELECT setval(pg_get_serial_sequence('companies', 'id'), COALESCE((SELECT MAX(id) FROM companies), 1));
SELECT setval(pg_get_serial_sequence('projects', 'id'), COALESCE((SELECT MAX(id) FROM projects), 1));
SELECT setval(pg_get_serial_sequence('goals', 'id'), COALESCE((SELECT MAX(id) FROM goals), 1));
SELECT setval(pg_get_serial_sequence('stages', 'id'), COALESCE((SELECT MAX(id) FROM stages), 1));
SELECT setval(pg_get_serial_sequence('phases', 'id'), COALESCE((SELECT MAX(id) FROM phases), 1));
SELECT setval(pg_get_serial_sequence('budget_categories', 'id'), COALESCE((SELECT MAX(id) FROM budget_categories), 1));
SELECT setval(pg_get_serial_sequence('budget_items', 'id'), COALESCE((SELECT MAX(id) FROM budget_items), 1));
SELECT setval(pg_get_serial_sequence('incomes', 'id'), COALESCE((SELECT MAX(id) FROM incomes), 1));
SELECT setval(pg_get_serial_sequence('disbursement_schedule', 'id'), COALESCE((SELECT MAX(id) FROM disbursement_schedule), 1));
SELECT setval(pg_get_serial_sequence('expenses', 'id'), COALESCE((SELECT MAX(id) FROM expenses), 1));
SELECT setval(pg_get_serial_sequence('budget_transfers', 'id'), COALESCE((SELECT MAX(id) FROM budget_transfers), 1));
SELECT setval(pg_get_serial_sequence('project_people', 'id'), COALESCE((SELECT MAX(id) FROM project_people), 1));
SELECT setval(pg_get_serial_sequence('project_company', 'id'), COALESCE((SELECT MAX(id) FROM project_company), 1));
