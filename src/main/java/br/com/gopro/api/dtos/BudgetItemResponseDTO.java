package br.com.gopro.api.dtos;


import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetItemResponseDTO(
        Long id,
        Long project,
        Long budgetCategories,
        String description,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal plannedAmount,
        BigDecimal executedAmount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
