package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record AuditLogResponseDTO(
        Long id,
        Long actorUserId,
        String actorEmail,
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
