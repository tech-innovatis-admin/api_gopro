package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record BudgetCategoryRequestDTO(
        Long projectId,
        String code,
        @NotBlank(message = "Nome é obrigatório") String name,
        String description,
        Long createdBy
) {}
