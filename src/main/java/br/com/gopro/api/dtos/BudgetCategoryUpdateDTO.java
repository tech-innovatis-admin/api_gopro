package br.com.gopro.api.dtos;

public record BudgetCategoryUpdateDTO(
        Long projectId,
        String code,
        String name,
        String description,
        Long updatedBy
) {}