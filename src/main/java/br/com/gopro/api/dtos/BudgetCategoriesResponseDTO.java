package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record BudgetCategoriesResponseDTO(
        Long id,
        String name,
        String description,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
