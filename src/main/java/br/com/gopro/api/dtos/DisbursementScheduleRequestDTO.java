package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusDisbursementScheduleEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DisbursementScheduleRequestDTO(
        @NotNull(message = "ID do projeto é obrigatório")
        Long project,

        @NotBlank(message = "Mês é obrigatório")
        LocalDate expectedMonth,

        @NotNull(message = "Valor é obrigatório")
        BigDecimal expectedAmount,

        @NotBlank(message = "Status é obrigatório")
        StatusDisbursementScheduleEnum statusDisbursementSchedule,
        String notes,
        Long createdBy,
        Long updatedBy
) {
}
