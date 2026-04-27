package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ProjectDeadlineTriggerRequestDTO(
        @NotNull(message = "Data de referencia e obrigatoria")
        LocalDate referenceDate
) {
}
