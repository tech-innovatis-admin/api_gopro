package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.BudgetTransferStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BudgetTransferResponseDTO(
        Long id,
        Long project,
        Long budgetItems,
        Long fromBudgetCategories,
        Long toBudgetCategories,
        BigDecimal amount,
        LocalDate transferDate,
        BudgetTransferStatusEnum budgetTransferStatus,
        String reason,
        DocumentRequestDTO document,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy,
        LocalDateTime approvedAt,
        Long approvedBy
) {
}
