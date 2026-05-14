package br.com.gopro.api.service;

import br.com.gopro.api.enums.ContractingStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCompanyFinancialValidationServiceImplTest {

    @Mock
    private ProjectCompanyRepository projectCompanyRepository;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ProjectCompanyFinancialValidationServiceImpl service;

    @Test
    void validateCanReceivePayment_shouldRejectCancelledCompany() {
        ProjectCompany projectCompany = projectCompany(10L, 1L, ContractingStatusEnum.CANCELADA, true, "1000.00");
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(projectCompany));

        assertThatThrownBy(() -> service.validateCanReceivePayment(1L, 10L, new BigDecimal("100.00"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao permite novos lancamentos financeiros");
    }

    @Test
    void validateCanLinkToBudgetItem_shouldRejectInactiveCompany() {
        ProjectCompany projectCompany = projectCompany(10L, 1L, ContractingStatusEnum.CONTRATADA, false, "1000.00");
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(projectCompany));

        assertThatThrownBy(() -> service.validateCanLinkToBudgetItem(1L, 10L, new BigDecimal("100.00"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("inativa");
    }

    @Test
    void validateCanReceivePayment_shouldRejectAmountOverAvailableBalance() {
        ProjectCompany projectCompany = projectCompany(10L, 1L, ContractingStatusEnum.CONTRATADA, true, "100.00");
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(projectCompany));
        when(budgetItemRepository.sumContractedAmountByProjectAndProjectCompanyIgnoringItem(1L, 10L, null))
                .thenReturn(new BigDecimal("80.00"));
        when(expenseRepository.sumAmountByProjectAndProjectCompanyIgnoringExpense(1L, 10L, null))
                .thenReturn(new BigDecimal("10.00"));

        assertThatThrownBy(() -> service.validateCanReceivePayment(1L, 10L, new BigDecimal("20.00"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");
    }

    @Test
    void validateCanReceivePayment_shouldAllowAmountWithinAvailableBalance() {
        ProjectCompany projectCompany = projectCompany(10L, 1L, ContractingStatusEnum.EM_EXECUCAO, true, "100.00");
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(projectCompany));
        when(budgetItemRepository.sumContractedAmountByProjectAndProjectCompanyIgnoringItem(1L, 10L, null))
                .thenReturn(new BigDecimal("60.00"));
        when(expenseRepository.sumAmountByProjectAndProjectCompanyIgnoringExpense(1L, 10L, null))
                .thenReturn(new BigDecimal("10.00"));

        assertThatCode(() -> service.validateCanReceivePayment(1L, 10L, new BigDecimal("20.00"), null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCanReceivePayment_shouldRejectCompanyFromAnotherProject() {
        ProjectCompany projectCompany = projectCompany(10L, 2L, ContractingStatusEnum.CONTRATADA, true, "1000.00");
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(projectCompany));

        assertThatThrownBy(() -> service.validateCanReceivePayment(1L, 10L, new BigDecimal("100.00"), null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("nao pertence ao projeto");
    }

    private ProjectCompany projectCompany(
            Long projectCompanyId,
            Long projectId,
            ContractingStatusEnum status,
            boolean active,
            String totalValue
    ) {
        Project project = new Project();
        project.setId(projectId);

        ProjectCompany projectCompany = new ProjectCompany();
        projectCompany.setId(projectCompanyId);
        projectCompany.setProject(project);
        projectCompany.setStatus(status);
        projectCompany.setIsActive(active);
        projectCompany.setTotalValue(new BigDecimal(totalValue));
        return projectCompany;
    }
}
