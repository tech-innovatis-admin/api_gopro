INSERT INTO partners (id, acronym, name, trade_name, partners_type, cnpj, email, phone, address, site, city, state, is_active, created_by, created_at)
VALUES
  (3, 'IFPA', 'Instituto Federal do Para', 'IFPA', 'IF', '34567890000121', 'contato@ifpa.edu.br', '9130000000', 'Av. do IFPA, 2000', 'https://ifpa.edu.br', 'Belem', 'PA', true, 1, CURRENT_TIMESTAMP),
  (4, 'IFPI', 'Instituto Federal do Piaui', 'IFPI', 'IF', '45678900000122', 'contato@ifpi.edu.br', '8630000000', 'Av. do IFPI, 3000', 'https://ifpi.edu.br', 'Teresina', 'PI', true, 1, CURRENT_TIMESTAMP),
  (5, 'FUND-TEC', 'Fundacao Tecnologica', 'Fundacao Tecnologica', 'FUNDACAO', '56789000000123', 'contato@fundtec.org', '9831112222', 'Rua da Inovacao, 123', 'https://fundtec.org', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (6, 'FUND-EDU', 'Fundacao Educacional', 'Fundacao Educacional', 'FUNDACAO', '67890000000124', 'contato@fundedu.org', '9832223333', 'Av. Educacao, 456', 'https://fundedu.org', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO public_agencies (id, code, sigla, name, cnpj, is_client, public_agency_type, email, phone, address, contact_person, city, state, is_active, created_by, created_at)
VALUES
  (4, 'PREF-IMP', 'PMI', 'Prefeitura Municipal de Imperatriz', '67890100000131', true, 'PREFEITURA', 'prefeitura@imperatriz.ma.gov.br', '9931111111', 'Rua do Centro, 10', 'Jose Almeida', 'Imperatriz', 'MA', true, 1, CURRENT_TIMESTAMP),
  (5, 'GOV-PA', 'GOVPA', 'Governo do Estado do Para', '78901000000132', true, 'GOVERNO_ESTADUAL', 'governo@pa.gov.br', '9132222222', 'Palacio do Governo, S/N', 'Mariana Souza', 'Belem', 'PA', true, 1, CURRENT_TIMESTAMP),
  (6, 'MDIC', 'MDIC', 'Ministerio do Desenvolvimento', '89010000000133', true, 'MINISTERIO', 'contato@mdic.gov.br', '6134444444', 'Esplanada, Bloco J', 'Carlos Mendes', 'Brasilia', 'DF', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO secretariats (id, code, sigla, public_agency_id, name, cnpj, is_client, email, phone, address, contact_person, is_active, created_by, created_at)
VALUES
  (4, 'SEMED-IMP', 'SEMED', 4, 'Secretaria Municipal de Educacao', '90100000000134', true, 'semed@imperatriz.ma.gov.br', '9933333333', 'Av. da Educacao, 80', 'Luciana Rocha', true, 1, CURRENT_TIMESTAMP),
  (5, 'SEMUS-IMP', 'SEMUS', 4, 'Secretaria Municipal de Saude', '01200000000135', true, 'semus@imperatriz.ma.gov.br', '9934444444', 'Rua da Saude, 90', 'Rafael Lima', true, 1, CURRENT_TIMESTAMP),
  (6, 'SECTI-PA', 'SECTI', 5, 'Secretaria de Ciencia e Tecnologia', '12300000000136', true, 'secti@pa.gov.br', '9135555555', 'Av. da Ciencia, 500', 'Paula Santos', true, 1, CURRENT_TIMESTAMP),
  (7, 'SEDUC-PA', 'SEDUC', 5, 'Secretaria de Educacao', '23400000000137', true, 'seduc@pa.gov.br', '9136666666', 'Av. da Educacao, 600', 'Bruno Mota', true, 1, CURRENT_TIMESTAMP),
  (8, 'SECAP-MDIC', 'SECAP', 6, 'Secretaria de Capacitacao', '34500000000138', true, 'secap@mdic.gov.br', '6137777777', 'Esplanada, Bloco J', 'Carla Reis', true, 1, CURRENT_TIMESTAMP),
  (9, 'SEPROD-MDIC', 'SEPROD', 6, 'Secretaria de Producao', '45600000000139', true, 'seprod@mdic.gov.br', '6138888888', 'Esplanada, Bloco J', 'Pedro Araujo', true, 1, CURRENT_TIMESTAMP),
  (10, 'SEJUV-PA', 'SEJUV', 5, 'Secretaria da Juventude', '56700000000140', true, 'sejuv@pa.gov.br', '9139999999', 'Av. da Juventude, 700', 'Juliana Pires', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO peoples (id, full_name, cpf, email, phone, birth_date, address, zip_code, city, state, is_active, created_by, created_at)
VALUES
  (3, 'Thiago Martins', '34567890123', 'thiago.martins@gopro.org', '9897777777', '1987-06-10', 'Rua A, 123', '65020000', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (4, 'Camila Nunes', '45678901234', 'camila.nunes@gopro.org', '9896666666', '1992-11-05', 'Rua B, 200', '65020001', 'Sao Luis', 'MA', true, 1, CURRENT_TIMESTAMP),
  (5, 'Rafael Dias', '56789012345', 'rafael.dias@gopro.org', '9195555555', '1984-03-22', 'Av. Central, 50', '66000000', 'Belem', 'PA', true, 1, CURRENT_TIMESTAMP),
  (6, 'Juliana Costa', '67890123456', 'juliana.costa@gopro.org', '9194444444', '1991-07-18', 'Rua das Flores, 10', '66000001', 'Belem', 'PA', true, 1, CURRENT_TIMESTAMP),
  (7, 'Paulo Henrique', '78901234567', 'paulo.henrique@gopro.org', '8693333333', '1980-01-30', 'Rua do Sol, 20', '64000000', 'Teresina', 'PI', true, 1, CURRENT_TIMESTAMP),
  (8, 'Aline Santos', '89012345678', 'aline.santos@gopro.org', '8692222222', '1995-09-12', 'Rua da Lua, 30', '64000001', 'Teresina', 'PI', true, 1, CURRENT_TIMESTAMP),
  (9, 'Mariana Freitas', '90123456789', 'mariana.freitas@gopro.org', '6131111111', '1988-12-01', 'SHS Quadra 1', '70000000', 'Brasilia', 'DF', true, 1, CURRENT_TIMESTAMP),
  (10, 'Joao Pedro', '01234567890', 'joao.pedro@gopro.org', '6130000000', '1993-04-25', 'SHN Quadra 2', '70000001', 'Brasilia', 'DF', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO organizations (id, name, trade_name, cnpj, type, email, phone, address, contact_person, zip_code, city, state, notes, is_active, created_by, created_at)
VALUES
  (3, 'Instituto de Inovacao Social', 'IIS', '67891000000141', 3, 'contato@iis.org', '9835550000', 'Rua Social, 77', 'Andre Lima', '65030000', 'Sao Luis', 'MA', 'Organizacao parceira', true, 1, CURRENT_TIMESTAMP),
  (4, 'Fundacao de Pesquisa Aplicada', 'FPA', '78910000000142', 0, 'contato@fpa.org', '9134440000', 'Av. Pesquisa, 88', 'Lucia Sousa', '66010000', 'Belem', 'PA', 'Fundacao de pesquisa', true, 1, CURRENT_TIMESTAMP),
  (5, 'Associacao Tecnologica', 'ATEC', '89100000000143', 2, 'contato@atec.org', '8633330000', 'Rua Tecnologia, 99', 'Renato Alves', '64010000', 'Teresina', 'PI', 'Associacao setorial', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO companies (id, name, trade_name, cnpj, email, phone, address, city, state, is_active, created_by, created_at)
VALUES
  (3, 'AmazTech LTDA', 'AmazTech', '77889900000150', 'contato@amaztech.com', '9131110000', 'Av. Amazonia, 500', 'Belem', 'PA', true, 1, CURRENT_TIMESTAMP),
  (4, 'Nordeste Sistemas LTDA', 'Nordeste Sistemas', '88990000000151', 'contato@nordestesistemas.com', '8631110000', 'Av. Nordeste, 600', 'Teresina', 'PI', true, 1, CURRENT_TIMESTAMP),
  (5, 'Capital Solucoes LTDA', 'Capital Solucoes', '99000000000152', 'contato@capitalsolucoes.com', '6131112222', 'Setor Comercial Sul, 700', 'Brasilia', 'DF', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO projects (id, name, code, project_status, area_segmento, "object", primary_partner_id, secundary_partner_id, primary_client_id, secundary_client_id, cordinator_id, project_gov_if, project_type, contract_value, start_date, end_date, opening_date, closing_date, city, state, execution_location, total_received, total_expenses, saldo, is_active, created_by, created_at)
VALUES
  (4, 'Inclusao Digital em Imperatriz', 'PROJ-DIG-004', 'EXECUCAO', 'Tecnologia', 'Laboratorios de informatica em escolas', 3, 5, 4, 4, 3, 'GOV', 'PROJETO', 1800000.00, '2025-05-01', '2027-05-01', '2025-06-01', NULL, 'Imperatriz', 'MA', 'Imperatriz - MA', 200000.00, 50000.00, 150000.00, true, 1, CURRENT_TIMESTAMP),
  (5, 'Programa Ciencia na Escola', 'PROJ-CIEN-005', 'PLANEJAMENTO', 'Educacao', 'Formacao docente e kits cientificos', 4, 6, 5, 6, 5, 'GOV', 'PROJETO', 2200000.00, '2025-08-01', '2028-08-01', '2025-09-01', NULL, 'Belem', 'PA', 'Belem - PA', 0.00, 0.00, 0.00, true, 1, CURRENT_TIMESTAMP),
  (6, 'Centros de Inovacao Regional', 'PROJ-INOV-006', 'EXECUCAO', 'Inovacao', 'Estruturacao de centros de inovacao', 5, 6, 6, 8, 9, 'GOV', 'PROJETO', 3500000.00, '2024-06-01', '2027-06-01', '2024-07-01', NULL, 'Brasilia', 'DF', 'Brasilia - DF', 800000.00, 200000.00, 600000.00, true, 1, CURRENT_TIMESTAMP),
  (7, 'Conectividade Rural', 'PROJ-RURAL-007', 'PLANEJAMENTO', 'Infraestrutura', 'Implantacao de rede em areas rurais', 3, 5, 4, 5, 4, 'GOV', 'PROJETO', 1200000.00, '2026-01-01', '2028-01-01', '2026-02-01', NULL, 'Imperatriz', 'MA', 'Zona Rural - MA', 0.00, 0.00, 0.00, true, 1, CURRENT_TIMESTAMP),
  (8, 'Capacitacao Industria 4.0', 'PROJ-IND-008', 'EXECUCAO', 'Industria', 'Cursos de automacao e IoT', 6, 5, 6, 9, 10, 'IF', 'PROJETO', 1600000.00, '2025-02-01', '2027-02-01', '2025-03-01', NULL, 'Brasilia', 'DF', 'Brasilia - DF', 400000.00, 80000.00, 320000.00, true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO goals (id, project_id, numero, titulo, descricao, data_inicio, data_fim, is_active, created_by, created_at)
VALUES
  (3, 4, 1, 'Montar laboratorios', 'Criar laboratorios com 20 computadores', '2025-06-01', '2026-06-01', true, 1, CURRENT_TIMESTAMP),
  (4, 6, 1, 'Estruturar centros', 'Implantar infraestrutura basica', '2024-07-01', '2025-12-31', true, 1, CURRENT_TIMESTAMP),
  (5, 8, 1, 'Formar profissionais', 'Capacitacao em automacao e IoT', '2025-03-01', '2026-12-31', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO stages (id, goal_id, numero, titulo, descricao, data_inicio, data_fim, is_active, created_by, created_at)
VALUES
  (4, 3, 1, 'Adequacao de salas', 'Obras e climatizacao', '2025-06-01', '2025-10-31', true, 1, CURRENT_TIMESTAMP),
  (5, 4, 1, 'Compra de mobiliario', 'Mobiliario e equipamentos', '2024-07-01', '2025-03-31', true, 1, CURRENT_TIMESTAMP),
  (6, 5, 1, 'Conteudo programatico', 'Elaboracao de ementas', '2025-03-01', '2025-09-30', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO phases (id, stage_id, numero, titulo, descricao, data_inicio, data_fim, is_active, created_by, created_at)
VALUES
  (4, 4, 1, 'Reforma', 'Reforma e pintura', '2025-06-01', '2025-08-15', true, 1, CURRENT_TIMESTAMP),
  (5, 5, 1, 'Compra', 'Processo de compra', '2024-07-01', '2024-12-31', true, 1, CURRENT_TIMESTAMP),
  (6, 6, 1, 'Definicao de trilhas', 'Trilhas de aprendizagem', '2025-03-01', '2025-06-30', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO budget_categories (id, project_id, code, name, description, is_active, created_by, created_at)
VALUES
  (4, 4, 'DIG-EQP', 'Equipamentos Digitais', 'Computadores e periféricos', true, 1, CURRENT_TIMESTAMP),
  (5, 6, 'INOV-OBR', 'Obras e Infraestrutura', 'Reformas e adequacoes', true, 1, CURRENT_TIMESTAMP),
  (6, 8, 'IND-CURS', 'Cursos e Capacitacao', 'Materiais e instrutores', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO budget_items (id, category_id, description, quantity, months, unit_cost, planned_amount, executed_amount, goal_id, notes, is_active, created_by, created_at)
VALUES
  (4, 4, 'Computadores desktop', 40, 1, 3500.00, 140000.00, 30000.00, 3, 'Laboratorios digitais', true, 1, CURRENT_TIMESTAMP),
  (5, 5, 'Mobiliario para centro', 1, 1, 90000.00, 90000.00, 20000.00, 4, 'Salas e auditórios', true, 1, CURRENT_TIMESTAMP),
  (6, 6, 'Instrutores externos', 6, 12, 8000.00, 576000.00, 0.00, 5, 'Contrato anual', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO incomes (id, project_id, numero, amount, received_at, source, invoice_number, notes, is_active, created_by, created_at)
VALUES
  (4, 4, 1, 200000.00, '2025-06-15', 'Prefeitura de Imperatriz', 'NF-IMP-001', 'Parcela inicial', true, 1, CURRENT_TIMESTAMP),
  (5, 6, 1, 500000.00, '2024-08-10', 'Ministerio do Desenvolvimento', 'NF-MDIC-001', 'Repasse inicial', true, 1, CURRENT_TIMESTAMP),
  (6, 8, 1, 400000.00, '2025-04-05', 'MDIC', 'NF-MDIC-002', 'Parcela unica', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO disbursement_schedule (id, project_id, numero, expected_month, expected_amount, status, notes, is_active, created_by, created_at)
VALUES
  (4, 4, 1, '2025-06-30', 200000.00, 'RECEBIDO', 'Repasse inicial', true, 1, CURRENT_TIMESTAMP),
  (5, 6, 1, '2024-08-31', 500000.00, 'RECEBIDO', 'Repasse inicial', true, 1, CURRENT_TIMESTAMP),
  (6, 8, 1, '2025-04-30', 400000.00, 'RECEBIDO', 'Parcela unica', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, owner_type, owner_id, category, original_name, content_type, size_bytes, sha256, bucket, s3_key, status, is_active, created_by, created_at)
VALUES
  ('55555555-5555-5555-5555-555555555555', 'PROJECT', 4, 'contrato', 'contrato_projeto_4.pdf', 'application/pdf', 204800, NULL, 'local-bucket', 'documents/PROJECT/4/contrato_projeto_4.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP),
  ('66666666-6666-6666-6666-666666666666', 'INCOME', 4, 'nota_fiscal', 'nf_parcela_4.pdf', 'application/pdf', 102400, NULL, 'local-bucket', 'documents/INCOME/4/nf_parcela_4.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP),
  ('77777777-7777-7777-7777-777777777777', 'EXPENSE', 3, 'recibo', 'recibo_lab_4.pdf', 'application/pdf', 51200, NULL, 'local-bucket', 'documents/EXPENSE/3/recibo_lab_4.pdf', 'AVAILABLE', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO expenses (id, budget_item_id, category_id, income_id, expense_date, quantity, amount, person_id, organization_id, description, invoice_number, invoice_date, document_id, is_active, created_by, created_at)
VALUES
  (3, 4, 4, 4, '2025-07-10', 10, 35000.00, NULL, 3, 'Compra de computadores', 'NF-EXP-003', '2025-07-05', '77777777-7777-7777-7777-777777777777', true, 1, CURRENT_TIMESTAMP),
  (4, 5, 5, 5, '2024-09-05', 1, 20000.00, 5, NULL, 'Adiantamento de mobiliario', 'NF-EXP-004', '2024-09-01', NULL, true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO budget_transfers (id, project_id, from_item_id, to_item_id, amount, transfer_date, status, reason, document_id, is_active, created_by, approved_at, approved_by, created_at)
VALUES
  (2, 4, 4, 5, 10000.00, '2025-08-20', 'APROVADO', 'Reforco para mobiliario', NULL, true, 1, '2025-08-25 10:00:00', 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_people (id, project_id, person_id, role, workload_hours, institutional_link, contract_type, start_date, end_date, status, base_amount, notes, is_active, created_by, created_at)
VALUES
  (3, 4, 3, 'DIRETOR', 20.00, 'IFPA', 'CLT', '2025-06-01', NULL, 'ATIVO', 11000.00, 'Coord. projeto digital', true, 1, CURRENT_TIMESTAMP),
  (4, 6, 9, 'DIRETOR', 30.00, 'MDIC', 'CLT', '2024-07-01', NULL, 'ATIVO', 15000.00, 'Coord. inovacao', true, 1, CURRENT_TIMESTAMP),
  (5, 8, 10, 'BOLSISTA', 12.00, 'IFMA', 'BOLSA', '2025-03-01', NULL, 'ATIVO', 3500.00, 'Apoio tecnico', true, 1, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO project_company (id, project_id, company_id, contract_number, description, start_date, end_date, status, total_value, notes, is_incubated, service_type, is_active, created_by, created_at)
VALUES
  (3, 4, 3, 'CT-2025-004', 'Fornecimento de computadores', '2025-06-15', '2026-06-14', 1, 140000.00, 'Entrega em 2 lotes', false, 'Fornecimento TI', true, 1, CURRENT_TIMESTAMP),
  (4, 6, 5, 'CT-2024-020', 'Servicos de consultoria', '2024-08-01', '2025-08-01', 1, 200000.00, 'Consultoria especializada', false, 'Consultoria', true, 1, CURRENT_TIMESTAMP)
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
