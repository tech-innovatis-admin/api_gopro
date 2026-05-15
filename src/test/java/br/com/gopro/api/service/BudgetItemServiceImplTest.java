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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    @Test
    void listAllBudgetItems_shouldKeepCurrentBehaviorWhenProjectCompanyFilterIsMissing() {
        BudgetItem item = savedItem(100L, 1L, 10L);
        when(budgetItemRepository.findByIsActiveTrueAndCategory_Project_Id(10L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));

        var response = service.listAllBudgetItems(0, 10, null, 10L, null);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).id()).isEqualTo(100L);
        verify(projectCompanyRepository, never()).existsById(any());
    }

    @Test
    void listAllBudgetItems_shouldFilterByProjectCompanyWhenOnlyProjectCompanyFilterIsPresent() {
        BudgetItem item = savedItem(100L, 1L, 10L);
        item.setProjectCompany(new br.com.gopro.api.model.ProjectCompany());
        item.getProjectCompany().setId(20L);
        when(projectCompanyRepository.existsById(20L)).thenReturn(true);
        when(budgetItemRepository.findByIsActiveTrueAndProjectCompany_Id(20L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1));

        var response = service.listAllBudgetItems(0, 10, null, null, 20L);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).projectCompanyId()).isEqualTo(20L);
    }

    @Test
    void listAllBudgetItems_shouldFilterByProjectAndProjectCompanyWhenBothFiltersArePresent() {
        when(projectCompanyRepository.existsById(20L)).thenReturn(true);
        when(budgetItemRepository.findByIsActiveTrueAndCategory_Project_IdAndProjectCompany_Id(10L, 20L, PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

        service.listAllBudgetItems(0, 10, null, 10L, 20L);

        verify(budgetItemRepository).findByIsActiveTrueAndCategory_Project_IdAndProjectCompany_Id(10L, 20L, PageRequest.of(0, 10));
    }

    @Test
    void listAllBudgetItems_shouldRejectUnknownProjectCompanyFilter() {
        when(projectCompanyRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> service.listAllBudgetItems(0, 10, null, null, 999L))
                .isInstanceOf(br.com.gopro.api.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Empresa vinculada ao projeto nao encontrada");
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

    private BudgetItem savedItem(Long itemId, Long categoryId, Long projectId) {
        BudgetItem item = new BudgetItem();
        item.setId(itemId);
        item.setCategory(category(categoryId, projectId));
        item.setDescription("Servico especializado");
        item.setQuantity(1);
        item.setMonths(1);
        item.setUnitCost(new BigDecimal("100.00"));
        item.setPlannedAmount(new BigDecimal("100.00"));
        item.setExecutedAmount(BigDecimal.ZERO);
        item.setIsActive(true);
        return item;
    }
}
