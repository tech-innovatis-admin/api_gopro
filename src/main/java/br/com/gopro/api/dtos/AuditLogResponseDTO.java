package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.AuditScopeEnum;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record AuditLogResponseDTO(
        Long id,
        String auditId,
        OffsetDateTime dataHora,
        AuditScopeEnum tipoAuditoria,
        String modulo,
        String feature,
        String entidadePrincipal,
        String aba,
        String subsecao,
        String resumo,
        String descricao,
        String resultado,
        String correlacaoId,
        Long usuarioResponsavelId,
        String usuarioResponsavelNome,
        String usuarioResponsavelEmail,
        String usuarioResponsavelRole,
        String alteracoesJson,
        String detalhesTecnicosJson,
        String action,
        String entityType,
        String entityId,
        String beforeJson,
        String afterJson,
        String ip,
        String userAgent,
        LocalDateTime createdAt
) {
}
