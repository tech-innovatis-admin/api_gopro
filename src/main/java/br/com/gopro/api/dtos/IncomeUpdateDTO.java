package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeUpdateDTO(
        Long projectId,
        Integer numero,
        BigDecimal amount,
        LocalDate receivedAt,
        String source,
        String invoiceNumber,
        String notes,
        Long updatedBy
) {}
