package br.com.gopro.api.dtos;

import java.math.BigDecimal;

public record BudgetItemUpdateDTO(
        Long categoryId,
        String description,
        Integer quantity,
        Integer months,
        BigDecimal unitCost,
        BigDecimal plannedAmount,
        BigDecimal executedAmount,
        Long goalId,
        String notes,
        Long updatedBy
) {}