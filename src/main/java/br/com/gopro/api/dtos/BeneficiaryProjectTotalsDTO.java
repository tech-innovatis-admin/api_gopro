package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record BeneficiaryProjectTotalsDTO(
        String beneficiaryType,
        Long beneficiaryId,
        String beneficiaryName,
        String beneficiaryRole,
        BigDecimal contractedAmountTotal,
        BigDecimal totalReceived,
        BigDecimal balance,
        BigDecimal percentExecuted,
        Boolean isOverBudget,
        List<BeneficiaryBudgetSummaryDTO> items
) {
}

