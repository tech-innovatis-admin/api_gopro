package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseUpdateDTO(
        Long projectId,
        Long budgetItemId,
        Long categoryId,
        Long incomeId,
        LocalDate expenseDate,
        Integer quantity,
        BigDecimal amount,
        Long personId,
        Long organizationId,
        String description,
        String invoiceNumber,
        LocalDate invoiceDate,
        UUID documentId,
        Long updatedBy
) {}
