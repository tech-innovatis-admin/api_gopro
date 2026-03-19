package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseRequestDTO(
        Long projectId,
        @NotNull(message = "Item orcamentario e obrigatorio") Long budgetItemId,
        @NotNull(message = "Categoria e obrigatoria") Long categoryId,
        Long incomeId,
        @NotNull(message = "Data da despesa e obrigatoria") LocalDate expenseDate,
        @NotNull(message = "Quantidade e obrigatoria") Integer quantity,
        @NotNull(message = "Valor e obrigatorio") BigDecimal amount,
        Long personId,
        Long organizationId,
        String description,
        String invoiceNumber,
        LocalDate invoiceDate,
        UUID documentId,
        Long createdBy
) {}
