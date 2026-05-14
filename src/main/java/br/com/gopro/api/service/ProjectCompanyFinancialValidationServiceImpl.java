package br.com.gopro.api.service;

import br.com.gopro.api.enums.ContractingStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProjectCompanyFinancialValidationServiceImpl implements ProjectCompanyFinancialValidationService {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private final ProjectCompanyRepository projectCompanyRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final ExpenseRepository expenseRepository;

    @Override
    public void validateCanLinkToBudgetItem(
            Long projectId,
            Long projectCompanyId,
            BigDecimal requestedAmount,
            Long ignoredBudgetItemId
    ) {
        ProjectCompany projectCompany = validateBase(projectId, projectCompanyId, requestedAmount);
        if (projectCompany == null) {
            return;
        }

        BigDecimal availableBalance = calculateAvailableBalance(projectCompany, projectId, projectCompanyId, ignoredBudgetItemId, null);
        validateBalance(availableBalance, requestedAmount);
    }

    @Override
    public void validateCanReceivePayment(
            Long projectId,
            Long projectCompanyId,
            BigDecimal requestedAmount,
            Long ignoredExpenseId
    ) {
        ProjectCompany projectCompany = validateBase(projectId, projectCompanyId, requestedAmount);
        if (projectCompany == null) {
            return;
        }

        BigDecimal availableBalance = calculateAvailableBalance(projectCompany, projectId, projectCompanyId, null, ignoredExpenseId);
        validateBalance(availableBalance, requestedAmount);
    }

    private ProjectCompany validateBase(Long projectId, Long projectCompanyId, BigDecimal requestedAmount) {
        if (projectCompanyId == null) {
            return null;
        }
        if (projectId == null || projectId <= 0) {
            throw new BusinessException("Projeto do lancamento e obrigatorio");
        }
        if (requestedAmount == null || requestedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor do lancamento deve ser maior que zero");
        }

        ProjectCompany projectCompany = projectCompanyRepository.findById(projectCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa vinculada ao projeto nao encontrada"));

        Long linkedProjectId = projectCompany.getProject() != null ? projectCompany.getProject().getId() : null;
        if (!projectId.equals(linkedProjectId)) {
            throw new BusinessException("Empresa contratada nao pertence ao projeto informado");
        }
        if (!Boolean.TRUE.equals(projectCompany.getIsActive())) {
            throw new BusinessException("Empresa contratada inativa nao pode receber novos vinculos financeiros");
        }

        ContractingStatusEnum status = projectCompany.getStatus();
        if (status == null || !status.allowsFinancialLink()) {
            throw new BusinessException("Empresa contratada com status " + status + " nao permite novos lancamentos financeiros");
        }

        return projectCompany;
    }

    private BigDecimal calculateAvailableBalance(
            ProjectCompany projectCompany,
            Long projectId,
            Long projectCompanyId,
            Long ignoredBudgetItemId,
            Long ignoredExpenseId
    ) {
        BigDecimal totalValue = safe(projectCompany.getTotalValue());
        BigDecimal contractedAmount = safe(budgetItemRepository.sumContractedAmountByProjectAndProjectCompanyIgnoringItem(
                projectId,
                projectCompanyId,
                ignoredBudgetItemId
        ));
        BigDecimal paidAmount = safe(expenseRepository.sumAmountByProjectAndProjectCompanyIgnoringExpense(
                projectId,
                projectCompanyId,
                ignoredExpenseId
        ));

        return totalValue.subtract(contractedAmount).subtract(paidAmount);
    }

    private void validateBalance(BigDecimal availableBalance, BigDecimal requestedAmount) {
        if (availableBalance.compareTo(requestedAmount) < 0) {
            throw new BusinessException("Saldo insuficiente para a empresa contratada. Saldo disponivel: " + formatCurrency(availableBalance));
        }
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String formatCurrency(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }
}
