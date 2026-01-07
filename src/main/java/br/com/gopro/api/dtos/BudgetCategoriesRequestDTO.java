package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record BudgetCategoriesRequestDTO(
        @NotBlank(message = "Nome do orçamento é obrigatório")
        String name,
        String description
) {
}
