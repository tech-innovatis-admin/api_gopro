package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ExpensePaymentStatusEnum;
import br.com.gopro.api.enums.ExpensePaidByEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpenseResponseDTO(
        Long id,
        Long projectId,
        Long budgetItemId,
        Long categoryId,
        Long incomeId,
        LocalDate expenseDate,
        Integer quantity,
        BigDecimal amount,
        ExpensePaymentStatusEnum paymentStatus,
        ExpensePaidByEnum paidBy,
        Long personId,
        Long organizationId,
        String description,
        String invoiceNumber,
        LocalDate invoiceDate,
        UUID documentId,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
