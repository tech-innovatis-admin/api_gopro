package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetItemRequestDTO(
        @NotNull(message = "Id do projeto é obrigatório")
        Long project,
        @NotNull(message = "Id do BudgetCategories é obrigatório")
        Long BudgetCategories,
        String description,
        Integer quantity,
        BigDecimal unitCost,
        BigDecimal plannedAmount,
        BigDecimal executedAmount,
        String notes
) {
}
