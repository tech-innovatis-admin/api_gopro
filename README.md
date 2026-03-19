# API GoPro 2

Backend Spring Boot da plataforma GoPro.

## Requisitos
- Java 17+
- Maven (ou `./mvnw`)
- PostgreSQL

## Executando localmente
1. Ajuste credenciais de banco em `src/main/resources/application.properties` (ou variaveis de ambiente).
2. Rode:

```bash
./mvnw spring-boot:run
```

Por padrao, o profile ativo e `dev` (`spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}`).

## Profiles e CORS
- `dev`: permite `http://localhost:3000`, `http://127.0.0.1:3000`, `http://localhost:5173`, `http://127.0.0.1:5173`.
- `prod`: usa `APP_CORS_ALLOWED_ORIGINS` (lista separada por virgula).

Arquivos:
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/resources/application-prod.properties`

## Flyway por ambiente
- `core` (schema/evolucao): `src/main/resources/db/migration/core`
- `dev` (seed de desenvolvimento): `src/main/resources/db/migration/dev`
- `prod` (bootstrap limpo para primeira subida): `src/main/resources/db/migration/prod`

Comportamento:
- `dev`: aplica `core` + `dev`
- `prod`: aplica apenas `prod`

Configuracoes:
- `application.properties`: `spring.flyway.locations=classpath:db/migration/core`
- `application-dev.properties`: `spring.flyway.locations=classpath:db/migration/core,classpath:db/migration/dev`
- `application-prod.properties`: `spring.flyway.locations=classpath:db/migration/prod`

## OpenAPI / Swagger
- UI: `http://localhost:8080/swagger-ui/index.html`
- Docs JSON: `http://localhost:8080/v3/api-docs`

## Observacoes
- Erros sao padronizados via `GlobalExceptionHandler`.

## Autenticacao, RBAC e Convites

### Endpoints publicos
- `POST /auth/login`
- `GET /register/validate?token=...`
- `POST /register/complete`
- `GET /health`

### Endpoints protegidos por role
- `SUPERADMIN`: `POST/GET/PATCH /admin/allowed-registrations/**`
- `SUPERADMIN`: `GET /admin/audit`
- `SUPERADMIN` e `ADMIN`: `GET/PATCH /admin/users/**`
- Demais rotas da API: autenticadas por JWT (`Authorization: Bearer <token>`)

### Fluxo de cadastro por convite
1. SUPERADMIN cria convite em `POST /admin/allowed-registrations`.
2. API retorna `inviteLink` com token temporario (token nunca e salvo em texto puro no banco).
3. Usuario abre o link, valida token em `GET /register/validate`.
4. Usuario conclui cadastro em `POST /register/complete`.

### Auditoria
Acoes sensiveis registradas em `audit_log`:
- login (sucesso/falha)
- criar/reemitir/cancelar convite
- concluir cadastro por convite
- atualizacao de role/status de usuario
- bootstrap de superadmin

Consulta disponivel em `GET /admin/audit` (apenas `SUPERADMIN`).

## Bootstrap seguro do SUPERADMIN

O superadmin inicial nao fica hardcoded no codigo. O bootstrap e controlado por env vars:

```bash
APP_AUTH_BOOTSTRAP_SUPERADMIN_ENABLED=true
APP_AUTH_BOOTSTRAP_SUPERADMIN_EMAIL=tech@innovatismc.com
APP_AUTH_BOOTSTRAP_SUPERADMIN_USERNAME=tech
APP_AUTH_BOOTSTRAP_SUPERADMIN_PASSWORD=InnovaLabs86@
APP_AUTH_BOOTSTRAP_SUPERADMIN_FULL_NAME=Tech Innovatis
```

Recomendado:
1. Habilitar `...ENABLED=true` apenas na primeira inicializacao controlada.
2. Subir a API e validar o login.
3. Voltar `...ENABLED=false` para evitar rebootstrap.

## Migrations da feature
- `core`: `V114__create_auth_and_audit_tables.sql`
- `prod`: `V026__create_auth_and_audit_tables.sql`
