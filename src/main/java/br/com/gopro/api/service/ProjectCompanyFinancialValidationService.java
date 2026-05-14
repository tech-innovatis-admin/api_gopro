package br.com.gopro.api.service;

import java.math.BigDecimal;

public interface ProjectCompanyFinancialValidationService {

    void validateCanLinkToBudgetItem(
            Long projectId,
            Long projectCompanyId,
            BigDecimal requestedAmount,
            Long ignoredBudgetItemId
    );

    void validateCanReceivePayment(
            Long projectId,
            Long projectCompanyId,
            BigDecimal requestedAmount,
            Long ignoredExpenseId
    );
}
