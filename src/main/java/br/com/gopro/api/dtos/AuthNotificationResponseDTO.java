package br.com.gopro.api.dtos;

import java.time.OffsetDateTime;

public record AuthNotificationResponseDTO(
        String id,
        String category,
        String severity,
        String title,
        String message,
        String href,
        Long contractId,
        OffsetDateTime occurredAt
) {
}
