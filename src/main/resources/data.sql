INSERT INTO partners (id, acronym, name, trade_name, partners_type, cnpj, email, phone, address, site, city, state, is_active, created_by)
VALUES
  (1, 'IFMA', 'Instituto Federal do Maranhao', 'IFMA', 'IF', '12345678000190', 'contato@ifma.edu.br', '9830000000', 'Av. do IFMA, 1000', 'https://ifma.edu.br', 'Sao Luis', 'MA', true, 1),
  (2, 'FUND-GOPRO', 'Fundacao GoPro', 'Fundacao GoPro', 'FUNDACAO', '23456789000100', 'fundacao@gopro.org', '9820000000', 'Rua das Fundacoes, 200', 'https://fundacaogopro.org', 'Sao Luis', 'MA', true, 1);

INSERT INTO public_agencies (id, code, sigla, name, cnpj, is_client, public_agency_type, email, phone, address, contact_person, city, state, is_active, created_by)
VALUES
  (1, 'PREF-SLZ', 'PMSL', 'Prefeitura Municipal de Sao Luis', '34567890000110', true, 'PREFEITURA', 'prefeitura@saoluis.ma.gov.br', '9831111111', 'Av. Pedro II, 100', 'Joao da Silva', 'Sao Luis', 'MA', true, 1),
  (2, 'GOV-MA', 'GOVMA', 'Governo do Estado do Maranhao', '45678900000120', true, 'GOVERNO_ESTADUAL', 'governo@ma.gov.br', '9832222222', 'Palacio dos Leoes, S/N', 'Maria Souza', 'Sao Luis', 'MA', true, 1),
  (3, 'MEC', 'MEC', 'Ministerio da Educacao', '56789000000130', true, 'MINISTERIO', 'contato@mec.gov.br', '6133333333', 'Esplanada dos Ministerios, Bloco L', 'Carlos Pereira', 'Brasilia', 'DF', true, 1);

INSERT INTO secretariats (id, code, sigla, public_agency_id, name, cnpj, is_client, email, phone, address, contact_person, is_active, created_by)
VALUES
  (1, 'SEMUS-SLZ', 'SEMUS', 1, 'Secretaria Municipal de Saude', '67890000000140', true, 'semus@saoluis.ma.gov.br', '9834444444', 'Rua da Saude, 50', 'Ana Lima', true, 1),
  (2, 'SEDUC-MA', 'SEDUC', 2, 'Secretaria de Estado da Educacao', '78900000000150', true, 'seduc@ma.gov.br', '9835555555', 'Av. Educacao, 500', 'Bruno Costa', true, 1),
  (3, 'SETEC-MEC', 'SETEC', 3, 'Secretaria de Educacao Profissional e Tecnologica', '89000000000160', true, 'setec@mec.gov.br', '6136666666', 'Esplanada, Bloco L', 'Paula Ramos', true, 1);

INSERT INTO peoples (id, full_name, cpf, email, phone, birth_date, address, zip_code, city, state, is_active, created_by)
VALUES
  (1, 'Marcos Andrade', '12345678901', 'marcos.andrade@gopro.org', '9899999999', '1985-04-20', 'Rua Central, 10', '65000000', 'Sao Luis', 'MA', true, 1),
  (2, 'Fernanda Oliveira', '23456789012', 'fernanda.oliveira@gopro.org', '9898888888', '1990-09-15', 'Av. Norte, 200', '65000001', 'Sao Luis', 'MA', true, 1);

INSERT INTO projects (id, name, code, project_status, area_segmento, object, primary_partner_id, secundary_partner_id, primary_client_id, secundary_client_id, cordinator_id, project_gov_if, project_type, contract_value, start_date, end_date, opening_date, closing_date, city, state, execution_location, total_received, total_expenses, saldo, is_active, created_by)
VALUES
  (1, 'Modernizacao de Unidades Basicas de Saude', 'PROJ-SAUDE-001', 'EXECUCAO', 'Saude', 'Modernizar UBSs com equipamentos e sistemas', 1, 2, 1, 1, 1, 'GOV', 'PROJETO', 2500000.00, '2025-01-10', '2026-12-31', '2025-02-01', NULL, 'Sao Luis', 'MA', 'Sao Luis - MA', 500000.00, 120000.00, 380000.00, true, 1),
  (2, 'Capacitacao Tecnologica em Escolas', 'PROJ-EDU-002', 'PLANEJAMENTO', 'Educacao', 'Formacao e laboratorios de tecnologia', 1, NULL, 2, 2, 2, 'GOV', 'PROJETO', 1500000.00, '2025-03-01', '2027-03-01', '2025-04-01', NULL, 'Sao Luis', 'MA', 'Sao Luis - MA', 0.00, 0.00, 0.00, true, 1),
  (3, 'Programa Nacional de Educacao Profissional', 'PROD-TEC-003', 'EXECUCAO', 'Educacao Profissional', 'Expansao de cursos tecnicos e laboratorios', 1, 2, 3, 3, 1, 'IF', 'PRODUTO', 5000000.00, '2024-08-01', '2026-08-01', '2024-09-01', NULL, 'Brasilia', 'DF', 'Brasilia - DF', 1200000.00, 300000.00, 900000.00, true, 1);
