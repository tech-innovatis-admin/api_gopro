package br.com.gopro.api.service.audit;

public record AuditFieldChange(
        String caminho,
        Object de,
        Object para,
        String tipo
) {
}

