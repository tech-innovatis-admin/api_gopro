package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetCategoryRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoryResponseDTO;
import br.com.gopro.api.dtos.BudgetCategoryUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.mapper.BudgetCategoryMapper;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetCategoryServiceImplTest {

    @Mock
    private BudgetCategoryRepository budgetCategoryRepository;

    @Mock
    private BudgetCategoryMapper budgetCategoryMapper;

    @InjectMocks
    private BudgetCategoryServiceImpl service;

    @Test
    void createBudgetCategory_shouldThrowBusinessException_whenNameAlreadyExistsInProject() {
        BudgetCategoryRequestDTO dto = requestDTO(10L, "Material de consumo");
        BudgetCategory budgetCategory = budgetCategory(null, 10L, "Material de consumo");

        when(budgetCategoryMapper.toEntity(dto)).thenReturn(budgetCategory);
        when(budgetCategoryRepository.existsByProject_IdAndName(10L, "Material de consumo")).thenReturn(true);

        assertThatThrownBy(() -> service.createBudgetCategory(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe uma rubrica com este nome neste contrato.");

        verify(budgetCategoryRepository, never()).save(any(BudgetCategory.class));
    }

    @Test
    void createBudgetCategory_shouldAllowSameNameWhenProjectIsDifferent() {
        BudgetCategoryRequestDTO dto = requestDTO(20L, "Material de consumo");
        BudgetCategory budgetCategory = budgetCategory(null, 20L, "Material de consumo");
        BudgetCategoryResponseDTO responseDTO = responseDTO(1L, 20L, "Material de consumo");

        when(budgetCategoryMapper.toEntity(dto)).thenReturn(budgetCategory);
        when(budgetCategoryRepository.existsByProject_IdAndName(20L, "Material de consumo")).thenReturn(false);
        when(budgetCategoryRepository.save(any(BudgetCategory.class))).thenAnswer(invocation -> {
            BudgetCategory saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(budgetCategoryMapper.toDTO(any(BudgetCategory.class))).thenReturn(responseDTO);

        BudgetCategoryResponseDTO result = service.createBudgetCategory(dto);

        assertThat(result).isEqualTo(responseDTO);

        ArgumentCaptor<BudgetCategory> captor = ArgumentCaptor.forClass(BudgetCategory.class);
        verify(budgetCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getProject().getId()).isEqualTo(20L);
        assertThat(captor.getValue().getName()).isEqualTo("Material de consumo");
        assertThat(captor.getValue().getIsActive()).isTrue();
    }

    @Test
    void updateBudgetCategoryById_shouldThrowBusinessException_whenNameAlreadyExistsInProject() {
        BudgetCategory existing = budgetCategory(5L, 10L, "Passagens");
        BudgetCategoryUpdateDTO dto = updateDTO(10L, "Material de consumo");

        when(budgetCategoryRepository.findById(5L)).thenReturn(Optional.of(existing));
        doAnswer(invocation -> {
            BudgetCategoryUpdateDTO update = invocation.getArgument(0);
            BudgetCategory budgetCategory = invocation.getArgument(1);
            budgetCategory.setProject(project(update.projectId()));
            budgetCategory.setName(update.name());
            budgetCategory.setUpdatedBy(update.updatedBy());
            return null;
        }).when(budgetCategoryMapper).updateEntityFromDTO(dto, existing);
        when(budgetCategoryRepository.existsByProject_IdAndNameAndIdNot(10L, "Material de consumo", 5L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.updateBudgetCategoryById(5L, dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Ja existe uma rubrica com este nome neste contrato.");

        verify(budgetCategoryRepository, never()).save(any(BudgetCategory.class));
    }

    private BudgetCategoryRequestDTO requestDTO(Long projectId, String name) {
        return new BudgetCategoryRequestDTO(projectId, "RUB-001", name, null, 1L);
    }

    private BudgetCategoryUpdateDTO updateDTO(Long projectId, String name) {
        return new BudgetCategoryUpdateDTO(projectId, "RUB-002", name, null, 2L);
    }

    private BudgetCategoryResponseDTO responseDTO(Long id, Long projectId, String name) {
        return new BudgetCategoryResponseDTO(id, projectId, "RUB-001", name, null, true, null, null, 1L, null);
    }

    private BudgetCategory budgetCategory(Long id, Long projectId, String name) {
        BudgetCategory budgetCategory = new BudgetCategory();
        budgetCategory.setId(id);
        budgetCategory.setProject(project(projectId));
        budgetCategory.setName(name);
        budgetCategory.setIsActive(true);
        return budgetCategory;
    }

    private Project project(Long id) {
        Project project = new Project();
        project.setId(id);
        return project;
    }
}
