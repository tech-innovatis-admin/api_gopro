package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeRequestDTO(
        @NotNull(message = "Projeto e obrigatorio") Long projectId,
        @NotNull(message = "Numero e obrigatorio") Integer numero,
        @NotNull(message = "Valor e obrigatorio") BigDecimal amount,
        @NotNull(message = "Data de recebimento e obrigatoria") LocalDate receivedAt,
        String source,
        String invoiceNumber,
        String notes,
        Long createdBy
) {}
