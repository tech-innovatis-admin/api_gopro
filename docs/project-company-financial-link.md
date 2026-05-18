# Empresa contratada em rubricas e pagamentos

`projectCompanyId` e o campo oficial para representar empresa contratada vinculada ao projeto nos fluxos financeiros.

`organizationId` em `expenses` permanece legado e nao deve ser usado para novos fluxos de empresa contratada. Nao houve backfill automatico de `organizationId` para `projectCompanyId`, porque nao existe regra confiavel para converter organizacao em empresa contratada vinculada ao projeto.

## Status de contratacao

`ProjectCompany.status` usa `ContractingStatusEnum`:

- `EM_CADASTRO`
- `EM_CONTRATACAO`
- `CONTRATADA`
- `EM_EXECUCAO`
- `CONCLUIDA`
- `CANCELADA`

Novos vinculos financeiros sao permitidos apenas para `CONTRATADA` e `EM_EXECUCAO`, desde que a empresa esteja ativa e tenha saldo disponivel.

Empresas `CANCELADA`, inativas ou com status preliminar nao podem receber novos vinculos em rubricas ou pagamentos.

## Saldo

O backend bloqueia por padrao lancamentos que excedam o saldo disponivel da empresa contratada no projeto.

Calculo aplicado:

```text
saldoDisponivel = project_company.total_value
                - soma(budget_items.contracted_amount ativos da empresa no projeto)
                - soma(expenses.amount ativos da empresa no projeto)
```

Em edicao, o registro atual e ignorado na soma para evitar bloqueio falso ao salvar um valor inalterado.

## DTO detalhado de empresa no projeto

`GET /project-companies/detailed` retorna tambem os campos financeiros agregados por `project_company_id`:

- `availableBalance`
- `executionPercentage`

Calculo aplicado no DTO detalhado:

```text
baseContratada = soma(coalesce(budget_items.contracted_amount, budget_items.planned_amount) ativos da empresa no projeto)
totalPago = soma(expenses.amount ativos da empresa no projeto)
availableBalance = baseContratada - totalPago
executionPercentage = (totalPago / baseContratada) * 100
```

Quando `baseContratada = 0`, `executionPercentage` retorna `null`.

## Rubricas

Itens de rubrica podem receber vinculo opcional com empresa contratada (`projectCompanyId`) ou pessoa vinculada ao projeto (`projectPeopleId`).

O vinculo e validado contra o projeto da categoria da rubrica. Quando o identificador nao existe ou pertence a outro projeto, a API retorna erro de validacao com `fieldErrors.projectCompanyId` ou `fieldErrors.projectPeopleId`.

Consulta por empresa contratada:

```text
GET /budget-items?projectCompanyId={id}
```

Sem o parametro `projectCompanyId`, a listagem mantem o comportamento anterior.

## Remocao de empresa contratada

A remocao/desativacao de `ProjectCompany` e bloqueada quando existir rubrica ou pagamento ativo vinculado por `projectCompanyId`.

Resposta esperada:

```text
409 Conflict
Nao e possivel remover esta empresa porque ela possui rubricas ou pagamentos vinculados.
```

## Rollback e legado

As novas colunas `expenses.project_company_id` e `budget_items.project_company_id` sao nullable para preservar dados existentes.

Rollback seguro de schema deve remover primeiro constraints e indices novos antes de remover a coluna `expenses.project_company_id`. A coluna `budget_items.project_company_id` pode existir em ambientes ja migrados por features anteriores e nao deve ser removida sem validar dependencias.
