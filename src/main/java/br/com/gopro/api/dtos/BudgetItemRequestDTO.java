package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetItemRequestDTO(
        @NotNull(message = "Categoria e obrigatoria") Long categoryId,
        @NotBlank(message = "Descricao e obrigatoria") String description,
        Integer quantity,
        Integer months,
        BigDecimal unitCost,
        @NotNull(message = "Valor planejado e obrigatorio") BigDecimal plannedAmount,
        BigDecimal executedAmount,
        Long goalId,
        String notes,
        Long createdBy
) {}