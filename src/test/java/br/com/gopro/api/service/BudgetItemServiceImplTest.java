package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.exception.FieldValidationException;
import br.com.gopro.api.mapper.BudgetItemMapper;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.GoalRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetItemServiceImplTest {

    @Mock
    private BudgetItemRepository budgetItemRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private BudgetCategoryRepository budgetCategoryRepository;

    @Mock
    private ProjectCompanyRepository projectCompanyRepository;

    @Mock
    private ProjectPeopleRepository projectPeopleRepository;

    @Mock
    private ProjectCompanyFinancialValidationService projectCompanyFinancialValidationService;

    private BudgetItemServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new BudgetItemServiceImpl(
                budgetItemRepository,
                new BudgetItemMapper(),
                goalRepository,
                budgetCategoryRepository,
                projectCompanyRepository,
                projectPeopleRepository,
                projectCompanyFinancialValidationService
        );
    }

    @Test
    void createBudgetItem_shouldPersistProjectCompanyWhenCompanyBelongsToSameProject() {
        when(budgetCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, 10L)));
        when(projectCompanyRepository.findProjectIdById(20L)).thenReturn(Optional.of(10L));
        when(budgetItemRepository.save(any(BudgetItem.class))).thenAnswer(invocation -> {
            BudgetItem item = invocation.getArgument(0);
            item.setId(100L);
            return item;
        });

        BudgetItemResponseDTO response = service.createBudgetItem(request(1L, null, 20L));

        ArgumentCaptor<BudgetItem> captor = ArgumentCaptor.forClass(BudgetItem.class);
        verify(budgetItemRepository).save(captor.capture());
        assertThat(captor.getValue().getProjectCompany().getId()).isEqualTo(20L);
        assertThat(response.projectCompanyId()).isEqualTo(20L);
    }

    @Test
    void createBudgetItem_shouldRejectProjectCompanyFromAnotherProjectWithFieldError() {
        when(budgetCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, 10L)));
        when(projectCompanyRepository.findProjectIdById(20L)).thenReturn(Optional.of(99L));

        assertThatThrownBy(() -> service.createBudgetItem(request(1L, null, 20L)))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> assertThat(((FieldValidationException) exception).getFieldErrors())
                        .containsEntry("projectCompanyId", "Empresa contratada nao pertence ao projeto informado."));
    }

    @Test
    void createBudgetItem_shouldPersistProjectPeopleWhenPersonBelongsToSameProject() {
        when(budgetCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, 10L)));
        when(projectPeopleRepository.findProjectIdById(30L)).thenReturn(Optional.of(10L));
        when(budgetItemRepository.save(any(BudgetItem.class))).thenAnswer(invocation -> {
            BudgetItem item = invocation.getArgument(0);
            item.setId(100L);
            return item;
        });

        BudgetItemResponseDTO response = service.createBudgetItem(request(1L, 30L, null));

        ArgumentCaptor<BudgetItem> captor = ArgumentCaptor.forClass(BudgetItem.class);
        verify(budgetItemRepository).save(captor.capture());
        assertThat(captor.getValue().getProjectPeople().getId()).isEqualTo(30L);
        assertThat(response.projectPeopleId()).isEqualTo(30L);
    }

    @Test
    void createBudgetItem_shouldRejectProjectPeopleFromAnotherProjectWithFieldError() {
        when(budgetCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, 10L)));
        when(projectPeopleRepository.findProjectIdById(30L)).thenReturn(Optional.of(99L));

        assertThatThrownBy(() -> service.createBudgetItem(request(1L, 30L, null)))
                .isInstanceOf(FieldValidationException.class)
                .satisfies(exception -> assertThat(((FieldValidationException) exception).getFieldErrors())
                        .containsEntry("projectPeopleId", "Pessoa vinculada nao pertence ao projeto informado."));
    }

    @Test
    void createBudgetItem_shouldAllowItemWithoutFinancialLink() {
        when(budgetCategoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, 10L)));
        when(budgetItemRepository.save(any(BudgetItem.class))).thenAnswer(invocation -> {
            BudgetItem item = invocation.getArgument(0);
            item.setId(100L);
            return item;
        });

        BudgetItemResponseDTO response = service.createBudgetItem(request(1L, null, null));

        assertThat(response.projectPeopleId()).isNull();
        assertThat(response.projectCompanyId()).isNull();
    }

    private BudgetItemRequestDTO request(Long categoryId, Long projectPeopleId, Long projectCompanyId) {
        return new BudgetItemRequestDTO(
                categoryId,
                "Servico especializado",
                1,
                1,
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                BigDecimal.ZERO,
                null,
                projectPeopleId,
                projectCompanyId,
                null,
                new BigDecimal("100.00"),
                null,
                1L
        );
    }

    private BudgetCategory category(Long categoryId, Long projectId) {
        Project project = new Project();
        project.setId(projectId);

        BudgetCategory category = new BudgetCategory();
        category.setId(categoryId);
        category.setProject(project);
        return category;
    }
}
