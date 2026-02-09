CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS partners (
    id bigserial PRIMARY KEY,
    acronym varchar(255),
    name varchar(255) NOT NULL,
    trade_name varchar(255) NOT NULL,
    partners_type varchar(50) NOT NULL,
    cnpj varchar(14) NOT NULL UNIQUE,
    email varchar(255) NOT NULL,
    phone varchar(50) NOT NULL,
    address varchar(400) NOT NULL,
    site varchar(300),
    city varchar(100) NOT NULL,
    state varchar(50) NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint NOT NULL,
    updated_by bigint
);

CREATE TABLE IF NOT EXISTS public_agencies (
    id bigserial PRIMARY KEY,
    code varchar(20) UNIQUE,
    sigla varchar(20),
    name varchar(255) NOT NULL,
    cnpj varchar(14) UNIQUE,
    is_client boolean NOT NULL,
    public_agency_type varchar(50) NOT NULL,
    email varchar(255),
    phone varchar(50),
    address varchar(400),
    contact_person varchar(255),
    city varchar(100),
    state varchar(50),
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint
);

CREATE TABLE IF NOT EXISTS secretariats (
    id bigserial PRIMARY KEY,
    code varchar(20) UNIQUE,
    sigla varchar(20),
    public_agency_id bigint NOT NULL,
    name varchar(255) NOT NULL,
    cnpj varchar(14),
    is_client boolean NOT NULL,
    email varchar(255),
    phone varchar(50),
    address varchar(400),
    contact_person varchar(255),
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_secretary_public_agency
        FOREIGN KEY (public_agency_id) REFERENCES public_agencies (id)
);

CREATE TABLE IF NOT EXISTS peoples (
    id bigserial PRIMARY KEY,
    full_name varchar(255) NOT NULL,
    cpf varchar(14) UNIQUE,
    email varchar(255),
    phone varchar(50),
    avatar_url varchar(500),
    birth_date date,
    address varchar(500),
    zip_code varchar(20),
    city varchar(100),
    state varchar(50),
    notes varchar(500),
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    update_at timestamp,
    created_by bigint,
    updated_by bigint
);

CREATE TABLE IF NOT EXISTS organizations (
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL,
    trade_name varchar(255),
    cnpj varchar(18) UNIQUE,
    type smallint,
    email varchar(255),
    phone varchar(50),
    address text,
    contact_person varchar(255),
    zip_code varchar(20),
    city varchar(100),
    state varchar(50),
    notes text,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint
);

CREATE TABLE IF NOT EXISTS companies (
    id bigserial PRIMARY KEY,
    name varchar(100) NOT NULL,
    trade_name varchar(100) NOT NULL,
    cnpj varchar(14) NOT NULL UNIQUE,
    email varchar(255) NOT NULL,
    phone varchar(50) NOT NULL,
    address varchar(400) NOT NULL,
    city varchar(100) NOT NULL,
    state varchar(50) NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint
);

CREATE TABLE IF NOT EXISTS projects (
    id bigserial PRIMARY KEY,
    name varchar(255) NOT NULL,
    code varchar(50) NOT NULL UNIQUE,
    project_status varchar(50) NOT NULL,
    area_segmento varchar(255),
    object text NOT NULL,
    primary_partner_id bigint NOT NULL,
    secundary_partner_id bigint NOT NULL,
    primary_client_id bigint NOT NULL,
    secundary_client_id bigint NOT NULL,
    cordinator_id bigint NOT NULL,
    project_gov_if varchar(50),
    project_type varchar(50),
    contract_value numeric(15,2),
    start_date date,
    end_date date,
    opening_date date,
    closing_date date,
    city varchar(255),
    state varchar(255),
    execution_location varchar(400),
    total_received numeric(35,2) NOT NULL DEFAULT 0,
    total_expenses numeric(35,2) NOT NULL DEFAULT 0,
    saldo numeric(35,2) NOT NULL DEFAULT 0,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp NOT NULL DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_project_primary_partner
        FOREIGN KEY (primary_partner_id) REFERENCES partners (id),
    CONSTRAINT fk_project_secundary_partner
        FOREIGN KEY (secundary_partner_id) REFERENCES partners (id),
    CONSTRAINT fk_project_primary_client
        FOREIGN KEY (primary_client_id) REFERENCES public_agencies (id),
    CONSTRAINT fk_project_secundary_client
        FOREIGN KEY (secundary_client_id) REFERENCES secretariats (id),
    CONSTRAINT fk_project_cordinator_id
        FOREIGN KEY (cordinator_id) REFERENCES peoples (id)
);

CREATE TABLE IF NOT EXISTS goals (
    id bigserial PRIMARY KEY,
    project_id bigint NOT NULL,
    numero integer NOT NULL,
    titulo varchar(255) NOT NULL,
    descricao text,
    data_inicio date,
    data_fim date,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_goal_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE IF NOT EXISTS stages (
    id bigserial PRIMARY KEY,
    goal_id bigint NOT NULL,
    numero integer NOT NULL,
    titulo varchar(255) NOT NULL,
    descricao text,
    data_inicio date,
    data_fim date,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_stage_goal_id
        FOREIGN KEY (goal_id) REFERENCES goals (id)
);

CREATE TABLE IF NOT EXISTS phases (
    id bigserial PRIMARY KEY,
    stage_id bigint NOT NULL,
    numero integer NOT NULL,
    titulo varchar(255) NOT NULL,
    descricao text,
    data_inicio date,
    data_fim date,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_phase_stage_id
        FOREIGN KEY (stage_id) REFERENCES stages (id)
);

CREATE TABLE IF NOT EXISTS budget_categories (
    id bigserial PRIMARY KEY,
    project_id bigint,
    code varchar(50) UNIQUE,
    name varchar(255) NOT NULL UNIQUE,
    description text,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    created_by bigint,
    updated_at timestamp,
    updated_by bigint,
    CONSTRAINT fk_budget_category_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE IF NOT EXISTS budget_items (
    id bigserial PRIMARY KEY,
    category_id bigint NOT NULL,
    description varchar(255) NOT NULL,
    quantity integer,
    months integer,
    unit_cost numeric(15,2),
    planned_amount numeric(15,2) NOT NULL,
    executed_amount numeric(15,2) NOT NULL DEFAULT 0,
    goal_id bigint,
    notes text,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_budget_item_category_id
        FOREIGN KEY (category_id) REFERENCES budget_categories (id),
    CONSTRAINT fk_budget_item_goal_id
        FOREIGN KEY (goal_id) REFERENCES goals (id)
);

CREATE TABLE IF NOT EXISTS incomes (
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
    created_by bigint NOT NULL,
    updated_by bigint,
    CONSTRAINT fk_income_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE IF NOT EXISTS disbursement_schedule (
    id bigserial PRIMARY KEY,
    project_id bigint NOT NULL,
    numero integer NOT NULL,
    expected_month date NOT NULL,
    expected_amount numeric(15,2) NOT NULL,
    status varchar(50) NOT NULL,
    notes text,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_disbursement_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id)
);

CREATE TABLE IF NOT EXISTS documents (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_type varchar(50) NOT NULL,
    owner_id bigint NOT NULL,
    category varchar(100),
    original_name varchar(255) NOT NULL,
    content_type varchar(100),
    size_bytes bigint NOT NULL,
    sha256 varchar(64),
    bucket varchar(255) NOT NULL,
    s3_key varchar(500) NOT NULL UNIQUE,
    status varchar(20) NOT NULL,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    deleted_at timestamp
);

CREATE INDEX IF NOT EXISTS idx_documents_owner ON documents (owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_documents_status ON documents (status);

CREATE TABLE IF NOT EXISTS expenses (
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

CREATE TABLE IF NOT EXISTS budget_transfers (
    id bigserial PRIMARY KEY,
    project_id bigint NOT NULL,
    from_item_id bigint NOT NULL,
    to_item_id bigint NOT NULL,
    amount numeric(15,2) NOT NULL,
    transfer_date date NOT NULL,
    status varchar(50) NOT NULL,
    reason text,
    document_id uuid,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    created_by bigint,
    updated_at timestamp,
    updated_by bigint,
    approved_at timestamp,
    approved_by bigint,
    CONSTRAINT fk_budget_transfer_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_budget_transfer_from_item_id
        FOREIGN KEY (from_item_id) REFERENCES budget_items (id),
    CONSTRAINT fk_budget_transfer_to_item_id
        FOREIGN KEY (to_item_id) REFERENCES budget_items (id),
    CONSTRAINT fk_budget_transfer_document_id
        FOREIGN KEY (document_id) REFERENCES documents (id)
);

CREATE TABLE IF NOT EXISTS project_people (
    id bigserial PRIMARY KEY,
    project_id bigint NOT NULL,
    person_id bigint NOT NULL,
    role varchar(255),
    workload_hours numeric(5,2),
    institutional_link varchar(255),
    contract_type varchar(50),
    start_date date,
    end_date date,
    status varchar(50),
    base_amount numeric(15,2),
    notes text,
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_project_people_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_people_person_id
        FOREIGN KEY (person_id) REFERENCES peoples (id)
);

CREATE TABLE IF NOT EXISTS project_company (
    id bigserial PRIMARY KEY,
    project_id bigint NOT NULL,
    company_id bigint NOT NULL,
    contract_number varchar(100),
    description text,
    start_date date,
    end_date date,
    status smallint,
    total_value numeric(15,2),
    notes text,
    is_incubated boolean,
    service_type varchar(255),
    is_active boolean NOT NULL DEFAULT true,
    created_at timestamp DEFAULT now(),
    updated_at timestamp,
    created_by bigint,
    updated_by bigint,
    CONSTRAINT fk_project_company_project_id
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_company_company_id
        FOREIGN KEY (company_id) REFERENCES companies (id)
);
