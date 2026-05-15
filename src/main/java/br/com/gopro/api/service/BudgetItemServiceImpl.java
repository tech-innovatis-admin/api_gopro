package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.FieldValidationException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.BudgetItemMapper;
import br.com.gopro.api.model.BudgetCategory;
import br.com.gopro.api.model.BudgetItem;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.GoalRepository;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetItemServiceImpl implements BudgetItemService {

    private final BudgetItemRepository budgetItemRepository;
    private final BudgetItemMapper budgetItemMapper;
    private final GoalRepository goalRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final ProjectCompanyRepository projectCompanyRepository;
    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectCompanyFinancialValidationService projectCompanyFinancialValidationService;

    @Override
    public BudgetItemResponseDTO createBudgetItem(BudgetItemRequestDTO dto) {
        BudgetItem budgetItem = budgetItemMapper.toEntity(dto);
        applyCategoryReferenceOnCreate(budgetItem, dto.categoryId());
        applyGoalReferenceOnCreate(budgetItem, dto.goalId());
        budgetItem.setIsActive(true);
        applyDefaults(budgetItem);
        validateProjectCompanyBudgetItem(budgetItem, null);
        BudgetItem saved = budgetItemRepository.save(budgetItem);
        return budgetItemMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<BudgetItemResponseDTO> listAllBudgetItems(
            int page,
            int size,
            Long categoryId,
            Long projectId,
            Long projectCompanyId
    ) {
        validatePage(page, size);
        validateProjectCompanyFilter(projectCompanyId);
        Pageable pageable = PageRequest.of(page, size);
        Page<BudgetItem> pageResult;
        if (categoryId != null && projectCompanyId != null) {
            pageResult = budgetItemRepository.findByIsActiveTrueAndCategory_IdAndProjectCompany_Id(categoryId, projectCompanyId, pageable);
        } else if (projectId != null && projectCompanyId != null) {
            pageResult = budgetItemRepository.findByIsActiveTrueAndCategory_Project_IdAndProjectCompany_Id(projectId, projectCompanyId, pageable);
        } else if (projectCompanyId != null) {
            pageResult = budgetItemRepository.findByIsActiveTrueAndProjectCompany_Id(projectCompanyId, pageable);
        } else if (categoryId != null) {
            pageResult = budgetItemRepository.findByIsActiveTrueAndCategory_Id(categoryId, pageable);
        } else if (projectId != null) {
            pageResult = budgetItemRepository.findByIsActiveTrueAndCategory_Project_Id(projectId, pageable);
        } else {
            pageResult = budgetItemRepository.findByIsActiveTrue(pageable);
        }
        List<BudgetItemResponseDTO> content = pageResult.getContent().stream()
                .map(budgetItemMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public BudgetItemResponseDTO findBudgetItemById(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new ResourceNotFoundException("Item orcamentario nao encontrado");
        }
        return budgetItemMapper.toDTO(budgetItem);
    }

    @Override
    public BudgetItemResponseDTO updateBudgetItemById(Long id, BudgetItemUpdateDTO dto) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um item inativo");
        }
        budgetItemMapper.updateEntityFromDTO(dto, budgetItem);
        applyCategoryReferenceOnUpdate(budgetItem, dto.categoryId());
        applyGoalReferenceOnUpdate(budgetItem, dto.goalId());
        applyDefaults(budgetItem);
        validateProjectCompanyBudgetItem(budgetItem, budgetItem.getId());
        BudgetItem updated = budgetItemRepository.save(budgetItem);
        return budgetItemMapper.toDTO(updated);
    }

    @Override
    public void deleteBudgetItemById(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (!Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Item orcamentario ja esta inativo");
        }
        budgetItem.setIsActive(false);
        budgetItemRepository.save(budgetItem);
    }

    @Override
    public BudgetItemResponseDTO restoreBudgetItemById(Long id) {
        BudgetItem budgetItem = budgetItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item orcamentario nao encontrado"));
        if (Boolean.TRUE.equals(budgetItem.getIsActive())) {
            throw new BusinessException("Item orcamentario ja esta ativo");
        }
        budgetItem.setIsActive(true);
        BudgetItem restored = budgetItemRepository.save(budgetItem);
        return budgetItemMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }

    private void validateProjectCompanyFilter(Long projectCompanyId) {
        if (projectCompanyId == null) {
            return;
        }
        if (projectCompanyId <= 0 || !projectCompanyRepository.existsById(projectCompanyId)) {
            throw new ResourceNotFoundException("Empresa vinculada ao projeto nao encontrada");
        }
    }

    private void applyDefaults(BudgetItem budgetItem) {
        if (budgetItem.getExecutedAmount() == null) {
            budgetItem.setExecutedAmount(BigDecimal.ZERO);
        }
    }

    private void applyCategoryReferenceOnCreate(BudgetItem budgetItem, Long categoryId) {
        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria orcamentaria nao encontrada"));
        budgetItem.setCategory(category);
    }

    private void applyCategoryReferenceOnUpdate(BudgetItem budgetItem, Long categoryId) {
        if (categoryId == null) {
            return;
        }

        BudgetCategory category = budgetCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria orcamentaria nao encontrada"));
        budgetItem.setCategory(category);
    }

    private void applyGoalReferenceOnCreate(BudgetItem budgetItem, Long goalId) {
        if (goalId == null) {
            budgetItem.setGoal(null);
            return;
        }

        budgetItem.setGoal(goalRepository.getReferenceById(goalId));
    }

    private void applyGoalReferenceOnUpdate(BudgetItem budgetItem, Long goalId) {
        if (goalId != null) {
            budgetItem.setGoal(goalRepository.getReferenceById(goalId));
            return;
        }

        if (budgetItem.getGoal() != null && budgetItem.getGoal().getId() == null) {
            budgetItem.setGoal(null);
        }
    }

    private void validateProjectCompanyBudgetItem(BudgetItem budgetItem, Long ignoredBudgetItemId) {
        Long projectId = budgetItem.getCategory() != null && budgetItem.getCategory().getProject() != null
                ? budgetItem.getCategory().getProject().getId()
                : null;
        validateBudgetItemLinks(projectId, budgetItem);

        projectCompanyFinancialValidationService.validateCanLinkToBudgetItem(
                projectId,
                budgetItem.getProjectCompany() != null ? budgetItem.getProjectCompany().getId() : null,
                budgetItem.getContractedAmount() != null ? budgetItem.getContractedAmount() : budgetItem.getPlannedAmount(),
                ignoredBudgetItemId
        );
    }

    private void validateBudgetItemLinks(Long projectId, BudgetItem budgetItem) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        Long projectPeopleId = budgetItem.getProjectPeople() != null ? budgetItem.getProjectPeople().getId() : null;
        Long projectCompanyId = budgetItem.getProjectCompany() != null ? budgetItem.getProjectCompany().getId() : null;

        if (projectPeopleId != null && projectCompanyId != null) {
            fieldErrors.put("projectPeopleId", "Informe somente pessoa ou empresa vinculada.");
            fieldErrors.put("projectCompanyId", "Informe somente pessoa ou empresa vinculada.");
        }

        if (projectPeopleId != null) {
            validateProjectPeopleLink(projectId, projectPeopleId, fieldErrors);
        }

        if (projectCompanyId != null) {
            validateProjectCompanyLink(projectId, projectCompanyId, fieldErrors);
        }

        if (!fieldErrors.isEmpty()) {
            throw new FieldValidationException("Existem campos invalidos no formulario.", fieldErrors);
        }
    }

    private void validateProjectPeopleLink(Long projectId, Long projectPeopleId, Map<String, String> fieldErrors) {
        if (projectId == null) {
            fieldErrors.putIfAbsent("projectPeopleId", "Nao foi possivel validar o vinculo da pessoa com o projeto.");
            return;
        }

        Long linkedProjectId = projectPeopleRepository.findProjectIdById(projectPeopleId).orElse(null);
        if (linkedProjectId == null) {
            fieldErrors.putIfAbsent("projectPeopleId", "Pessoa vinculada ao projeto nao encontrada.");
            return;
        }

        if (!projectId.equals(linkedProjectId)) {
            fieldErrors.putIfAbsent("projectPeopleId", "Pessoa vinculada nao pertence ao projeto informado.");
        }
    }

    private void validateProjectCompanyLink(Long projectId, Long projectCompanyId, Map<String, String> fieldErrors) {
        if (projectId == null) {
            fieldErrors.putIfAbsent("projectCompanyId", "Nao foi possivel validar a empresa contratada com o projeto.");
            return;
        }

        Long linkedProjectId = projectCompanyRepository.findProjectIdById(projectCompanyId).orElse(null);
        if (linkedProjectId == null) {
            fieldErrors.putIfAbsent("projectCompanyId", "Empresa vinculada ao projeto nao encontrada.");
            return;
        }

        if (!projectId.equals(linkedProjectId)) {
            fieldErrors.putIfAbsent("projectCompanyId", "Empresa contratada nao pertence ao projeto informado.");
        }
    }
}
