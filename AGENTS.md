# AGENTS.md — Backend

## Modo de trabalho

- Responda sempre em português do Brasil.
- Use tom pragmático, objetivo e técnico.
- Evite explicação escolar; priorize impacto real, risco, trade-off e próximo passo.
- Em tarefas médias ou maiores, apresente um plano curto antes de implementar.
- Quando houver ambiguidade relevante, pare e pergunte antes de alterar código, contrato, regra de negócio ou comportamento operacional.

## Ambiente operacional

- Assuma Windows + PowerShell como ambiente padrão.
- Use paths, comandos e convenções de Windows/PowerShell por padrão.
- Só use Bash/Linux quando estiver explicitamente dentro de Docker, container, WSL ou CI Unix.
- Use a toolchain já adotada pelo projeto; se houver wrapper oficial do build, prefira o wrapper existente.

## Git e worktrees

- Atue no worktree atual por padrão.
- Nunca presuma árvore limpa.
- Nunca reverta, sobrescreva ou reorganize alterações que não foram pedidas.
- Respeite worktrees paralelos e mantenha diffs pequenos, cirúrgicos e fáceis de revisar.
- Não faça refatoração ampla por impulso.

## Fonte de verdade

- Antes de concluir qualquer diagnóstico, inspecione código, configuração, variáveis de ambiente e comportamento observável.
- Quando documentação e código divergirem:
  - sinalize claramente a divergência;
  - descreva o que o código/runtime realmente fazem;
  - consulte o usuário antes de alterar comportamento com base nesse conflito.
- Para diagnóstico, privilegie o comportamento real da aplicação sobre documentação possivelmente desatualizada.

## Contexto arquitetural do backend

Considere como contexto padrão deste serviço:

- Backend em Spring Boot.
- Java 17.
- PostgreSQL.
- Flyway com comportamento por profile.
- Autenticação/autorização com JWT.
- Fluxos de convite.
- RBAC.
- Auditoria.
- Bucket S3 e integrações dependentes de ambiente.
- Deploy containerizado.

Ao propor ou implementar mudanças, considere sempre os impactos em:

- contrato de API;
- autenticação/autorização;
- CORS;
- variáveis de ambiente;
- profiles Spring;
- banco e migrações;
- deploy Docker;
- integração com o front via BFF `/api/backend`.

Não trate o backend como isolado. Mudanças relevantes devem ser pensadas ponta a ponta.

## Estilo de implementação

- Prefira mudanças pequenas, localizadas e de baixo risco.
- Preserve padrões já existentes antes de introduzir abstrações novas.
- Não introduza dependências novas sem necessidade clara e justificativa explícita.
- Não crie camadas, builders, helpers ou frameworks internos desnecessários.
- Preserve a linguagem do domínio já usada no projeto.

### Diretrizes de código

- Controllers devem permanecer enxutos.
- Regras de negócio devem ficar concentradas em serviços/casos de uso apropriados.
- Repositórios/queries devem ser explícitos, previsíveis e eficientes.
- Validação de entrada deve ser clara e consistente.
- Mapeamento de erros deve preservar semântica correta de status HTTP.
- Considere sempre transação, concorrência, idempotência e observabilidade quando relevante.

## API e compatibilidade

- Mantenha compatibilidade retroativa por padrão.
- Não altere contrato público sem deixar explícito:
  - endpoints afetados;
  - payloads alterados;
  - campos adicionados/removidos;
  - mudança de status codes;
  - impacto em autenticação/permissão;
  - impacto no BFF/front.
- Em mudanças de serialização, DTO, enum, paginação, filtros ou ordenação, pense no impacto em consumidores existentes.
- Em mudanças de 401/403/404/422/500, valide a semântica e o efeito no fluxo do front.

## Segurança

- Nunca exponha, copie ou repita segredos, credenciais, tokens, chaves, cookies, headers sensíveis ou valores de `.env`, `.env.local`, `.env.example`, `application*.properties`, secrets de CI/CD e similares.
- Ao resumir configuração, descreva sem ecoar valores sensíveis.
- Nunca enfraqueça JWT, RBAC, auditoria, validações ou controles de acesso por conveniência.
- Qualquer mudança em autenticação, autorização, permissões, claims, escopos, expiração, filtros ou interceptadores deve ser tratada como sensível e destacada explicitamente na entrega.
- Logs e mensagens de erro não devem vazar dados sensíveis.

## Banco de dados e Flyway

Mudanças de banco exigem postura conservadora.

- Prefira alterações aditivas e compatíveis com produção.
- Não edite migrações Flyway já aplicadas, salvo instrução explícita do usuário e consciência do impacto.
- Crie novas migrações claras, pequenas e revisáveis.
- Sempre considere:
  - impacto em volume real de dados;
  - locks;
  - tempo de execução;
  - compatibilidade entre versões em rollout;
  - necessidade de backfill;
  - estratégia de mitigação/rollback.
- Antes de mudar schema, valide o impacto em índices, constraints, FK, dados legados e queries existentes.
- Não assuma que uma mudança “funciona localmente” sem impacto operacional.

## Perfis, configuração e ambientes

- Respeite a organização atual de profiles Spring e configuração por ambiente.
- Não duplique configuração sem necessidade.
- Não hardcode valores de ambiente.
- Ao alterar config, considere local/dev/homolog/prod e efeitos colaterais no container/runtime.
- Sempre destaque novas variáveis, mudanças de profile, portas, dependências externas e comportamento condicional por ambiente.

## Docker e operação

- Considere sempre que o serviço é entregue de forma containerizada.
- Sempre avalie impacto em:
  - Dockerfile;
  - build multi-stage;
  - artefato final;
  - JRE/runtime;
  - portas expostas;
  - envs necessárias;
  - health/readiness quando aplicável;
  - startup/migrations em container.
- Não proponha deploy “mágico” de plataforma sem aderência explícita à arquitetura atual.

## Auditoria e rastreabilidade

- Preserve trilhas de auditoria já existentes.
- Se a mudança afetar eventos auditáveis, registre isso explicitamente.
- Não remova ou silencie auditoria sem necessidade clara e sem destacar o impacto.
- Em fluxos críticos, prefira rastreabilidade explícita a “mágica”.

## Integrações externas

Para integrações como S3, auth, mensageria, webhooks ou serviços terceiros:

- trate timeout, retry, erro transitório e observabilidade com cuidado;
- não suponha disponibilidade perfeita;
- preserve comportamento por ambiente;
- destaque efeitos colaterais e dependências externas;
- não exponha endpoints, buckets, chaves ou credenciais em respostas.

## Validação

Validação mínima esperada:

- lint/checkstyle/spotless/formatters, quando existirem no projeto;
- build do módulo afetado;
- testes relevantes, quando existirem ou forem impactados.

Quando aplicável, valide também:

- inicialização da aplicação;
- profiles/configuração alterados;
- migrações Flyway;
- segurança/autorização;
- serialização/deserialização;
- integração com banco;
- impacto em contratos HTTP.

Ao finalizar, informe claramente:

1. o que mudou;
2. o que foi validado de verdade;
3. o que não foi validado;
4. riscos;
5. próximos passos.

Não diga que “está tudo certo” sem dizer exatamente o que foi executado e observado.

## Documentação

- Sempre que alterar fluxo, regra de negócio, contrato de API, autenticação/autorização, env relevante, operação ou comportamento de deploy, atualize a documentação local correspondente.
- Se houver conflito entre docs e comportamento real, registre isso com clareza.
- Não deixe mudança estrutural sem documentação mínima.

## Formato da resposta final

Estruture a entrega final, sempre que fizer sentido, em:

1. plano curto;
2. alterações realizadas;
3. validação executada;
4. riscos e impactos;
5. próximos passos.

## O que evitar

- Refatoração ampla sem pedido explícito.
- Mudança silenciosa de contrato.
- Mudança de permissão/autorização sem destaque.
- Hardcode de configuração.
- Exposição de segredos.
- Alteração destrutiva de banco sem necessidade.
- “Conserto rápido” que aumenta risco operacional.