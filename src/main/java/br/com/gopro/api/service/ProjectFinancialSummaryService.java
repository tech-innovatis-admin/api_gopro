package br.com.gopro.api.service;

import br.com.gopro.api.enums.ExpensePaymentStatusEnum;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProjectFinancialSummaryService {

    private final ProjectRepository projectRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetItemRepository budgetItemRepository;

    @Transactional
    public void refreshIncomeAggregates(Long currentProjectId, Long previousProjectId) {
        distinctIds(previousProjectId, currentProjectId).forEach(this::refreshProjectFinancials);
    }

    @Transactional
    public void refreshExpenseAggregates(
            Long currentProjectId,
            Long previousProjectId,
            Long currentBudgetItemId,
            Long previousBudgetItemId
    ) {
        distinctIds(previousBudgetItemId, currentBudgetItemId).forEach(this::refreshBudgetItemExecutedAmount);
        distinctIds(previousProjectId, currentProjectId).forEach(this::refreshProjectFinancials);
    }

    @Transactional
    public void refreshProjectFinancials(Long projectId) {
        if (projectId == null) {
            return;
        }

        projectRepository.findById(projectId).ifPresent(project -> {
            BigDecimal totalReceived = incomeRepository.sumIncomeByProjectId(projectId);
            BigDecimal totalPaid = expenseRepository.sumExpenseByProjectIdAndPaymentStatus(
                    projectId,
                    ExpensePaymentStatusEnum.PAGO
            );
            BigDecimal totalReserved = expenseRepository.sumExpenseByProjectIdAndPaymentStatus(
                    projectId,
                    ExpensePaymentStatusEnum.RESERVADO
            );

            BigDecimal saldoReal = totalReceived.subtract(totalPaid);
            BigDecimal saldoProjeto = saldoReal.subtract(totalReserved);

            project.setTotalReceived(totalReceived);
            project.setTotalExpenses(totalPaid);
            project.setTotalReserved(totalReserved);
            project.setSaldoReal(saldoReal);
            project.setSaldo(saldoProjeto);

            projectRepository.save(project);
        });
    }

    @Transactional
    public void refreshBudgetItemExecutedAmount(Long budgetItemId) {
        if (budgetItemId == null) {
            return;
        }

        budgetItemRepository.findById(budgetItemId).ifPresent(budgetItem -> {
            budgetItem.setExecutedAmount(expenseRepository.sumExpenseByBudgetItemId(budgetItemId));
            budgetItemRepository.save(budgetItem);
        });
    }

    private Set<Long> distinctIds(Long... ids) {
        return Stream.of(ids)
                .filter(Objects::nonNull)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }
}
