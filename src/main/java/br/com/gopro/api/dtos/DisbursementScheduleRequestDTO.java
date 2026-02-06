package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusDisbursementScheduleEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DisbursementScheduleRequestDTO(
        @NotNull(message = "Projeto e obrigatorio") Long projectId,
        @NotNull(message = "Numero e obrigatorio") Integer numero,
        @NotNull(message = "Mes esperado e obrigatorio") LocalDate expectedMonth,
        @NotNull(message = "Valor esperado e obrigatorio") BigDecimal expectedAmount,
        @NotNull(message = "Status e obrigatorio") StatusDisbursementScheduleEnum status,
        String notes,
        Long createdBy
) {}