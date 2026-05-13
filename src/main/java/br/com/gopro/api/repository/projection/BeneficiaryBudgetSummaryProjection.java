package br.com.gopro.api.repository.projection;

import java.math.BigDecimal;

public interface BeneficiaryBudgetSummaryProjection {
    Long getBudgetItemId();
    Long getProjectId();
    Long getCategoryId();
    String getBudgetItemDescription();
    String getBeneficiaryType();
    Long getProjectPeopleId();
    Long getProjectCompanyId();
    String getBeneficiaryName();
    String getBeneficiaryRole();
    BigDecimal getContractedAmount();
    BigDecimal getPlannedAmount();
    BigDecimal getTotalReceived();
    BigDecimal getBalance();
    BigDecimal getPercentExecuted();
    Boolean getIsOverBudget();
}

