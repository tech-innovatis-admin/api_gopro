package br.com.gopro.api.dtos;

import java.time.LocalDate;

public record ProjectDeadlineTriggerResponseDTO(
        LocalDate referenceDate,
        int projectsEvaluated,
        int recipientsEvaluated,
        int emailsSent,
        int emailsSkipped,
        boolean notificationsEnabled,
        String message
) {
}
