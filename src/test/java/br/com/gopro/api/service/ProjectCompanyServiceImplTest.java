package br.com.gopro.api.service;

import br.com.gopro.api.exception.ConflictException;
import br.com.gopro.api.mapper.ProjectCompanyMapper;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectCompanyServiceImplTest {

    @Mock
    private ProjectCompanyRepository projectCompanyRepository;

    @Mock
    private ProjectCompanyMapper projectCompanyMapper;

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    private ProjectCompanyServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectCompanyServiceImpl(
                projectCompanyRepository,
                projectCompanyMapper,
                budgetItemRepository,
                expenseRepository
        );
    }

    @Test
    void deleteProjectCompanyById_shouldBlockWhenBudgetItemsAreLinked() {
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(activeProjectCompany(10L)));
        when(budgetItemRepository.existsByProjectCompany_IdAndIsActiveTrue(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteProjectCompanyById(10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("rubricas ou pagamentos vinculados");
        verify(projectCompanyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteProjectCompanyById_shouldBlockWhenExpensesAreLinked() {
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(activeProjectCompany(10L)));
        when(budgetItemRepository.existsByProjectCompany_IdAndIsActiveTrue(10L)).thenReturn(false);
        when(expenseRepository.existsByProjectCompany_IdAndIsActiveTrue(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.deleteProjectCompanyById(10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("rubricas ou pagamentos vinculados");
        verify(projectCompanyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteProjectCompanyById_shouldKeepCurrentFlowWhenThereAreNoFinancialLinks() {
        ProjectCompany projectCompany = activeProjectCompany(10L);
        when(projectCompanyRepository.findById(10L)).thenReturn(Optional.of(projectCompany));
        when(budgetItemRepository.existsByProjectCompany_IdAndIsActiveTrue(10L)).thenReturn(false);
        when(expenseRepository.existsByProjectCompany_IdAndIsActiveTrue(10L)).thenReturn(false);

        service.deleteProjectCompanyById(10L);

        assertThat(projectCompany.getIsActive()).isFalse();
        verify(projectCompanyRepository).save(projectCompany);
    }

    private ProjectCompany activeProjectCompany(Long id) {
        ProjectCompany projectCompany = new ProjectCompany();
        projectCompany.setId(id);
        projectCompany.setIsActive(true);
        return projectCompany;
    }
}
