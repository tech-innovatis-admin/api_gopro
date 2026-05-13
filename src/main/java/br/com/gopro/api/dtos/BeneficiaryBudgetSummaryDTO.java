package br.com.gopro.api.dtos;

import java.math.BigDecimal;

public record BeneficiaryBudgetSummaryDTO(
        Long budgetItemId,
        Long projectId,
        Long categoryId,
        String budgetItemDescription,
        String beneficiaryType,
        Long projectPeopleId,
        Long projectCompanyId,
        String beneficiaryName,
        String beneficiaryRole,
        BigDecimal contractedAmount,
        BigDecimal plannedAmount,
        BigDecimal totalReceived,
        BigDecimal balance,
        BigDecimal percentExecuted,
        Boolean isOverBudget
) {
}

