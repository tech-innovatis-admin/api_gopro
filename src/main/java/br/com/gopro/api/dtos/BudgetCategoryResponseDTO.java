package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record BudgetCategoryResponseDTO(
        Long id,
        Long projectId,
        String code,
        String name,
        String description,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}