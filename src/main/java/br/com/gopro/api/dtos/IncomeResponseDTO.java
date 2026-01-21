package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncomeResponseDTO(
        Long id,
        Long project,
        Long disbursementSchedule,
        Integer installment,
        BigDecimal amount,
        LocalDate receivedAt,
        String source,
        String invoiceNumber,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
}
