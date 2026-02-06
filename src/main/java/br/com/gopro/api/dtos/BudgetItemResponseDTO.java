package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetItemResponseDTO(
        Long id,
        Long categoryId,
        String description,
        Integer quantity,
        Integer months,
        BigDecimal unitCost,
        BigDecimal plannedAmount,
        BigDecimal executedAmount,
        Long goalId,
        String notes,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}