package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeRequestDTO(
        @NotNull(message = "ID do projeto é obrigatório")
        Long project,

        @NotNull(message = "ID do cronograma é obrigatório")
        Long disbursementSchedule,

        @NotNull(message = "Número da parcela é obrigatório")
        Integer installment,

        @NotNull(message = "Total é obrigatório")
        BigDecimal amount,

        @NotNull(message = "Data de recebimento é obrigatório")
        LocalDate receivedAt,
        String source,
        String invoiceNumber,
        String notes,
        Long createdBy
) {
}
