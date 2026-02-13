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
- Seguranca atual esta aberta (`permitAll`) para todas as rotas da API.
- Erros sao padronizados via `GlobalExceptionHandler`.
