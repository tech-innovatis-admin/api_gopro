package br.com.gopro.api.service.audit;

public record AuditMessage(
        String resumo,
        String descricao
) {
}

