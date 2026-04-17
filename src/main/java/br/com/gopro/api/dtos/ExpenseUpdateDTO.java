package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ExpensePaymentStatusEnum;
import br.com.gopro.api.enums.ExpensePaidByEnum;

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
        ExpensePaymentStatusEnum paymentStatus,
        ExpensePaidByEnum paidBy,
        Long personId,
        Long organizationId,
        String description,
        String invoiceNumber,
        LocalDate invoiceDate,
        UUID documentId,
        Long updatedBy
) {}
