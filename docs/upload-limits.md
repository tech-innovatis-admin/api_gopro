# Limite de upload de documentos

## Fonte unica de configuracao

- Propriedade principal: `app.documents.max-file-size`
- Variavel de ambiente: `APP_DOCUMENTS_MAX_FILE_SIZE`
- Valor padrao: `200MB`

## Alinhamento com multipart

As propriedades de multipart usam a mesma fonte:

- `spring.servlet.multipart.max-file-size=${app.documents.max-file-size}`
- `spring.servlet.multipart.max-request-size=${app.documents.max-file-size}`

## Comportamento de erro

- Arquivo acima do limite no servico de documentos:
  - `400 Bad Request`
  - mensagem: `O arquivo excede o limite maximo permitido de X MB.`
- Arquivo bloqueado pelo parser multipart:
  - `413 Payload Too Large`
  - mesma mensagem amigavel, sem detalhes tecnicos.
