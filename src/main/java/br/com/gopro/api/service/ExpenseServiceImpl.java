package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ExpenseMapper;
import br.com.gopro.api.model.Expense;
import br.com.gopro.api.repository.BudgetCategoryRepository;
import br.com.gopro.api.repository.BudgetItemRepository;
import br.com.gopro.api.repository.DocumentRepository;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.OrganizationRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final BudgetItemRepository budgetItemRepository;
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final IncomeRepository incomeRepository;
    private final PeopleRepository peopleRepository;
    private final OrganizationRepository organizationRepository;
    private final DocumentRepository documentRepository;
    private final ProjectRepository projectRepository;
    private final ProjectFinancialSummaryService projectFinancialSummaryService;

    @Override
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        applyReferencesOnCreate(expense, dto);
        expense.setIsActive(true);
        Expense saved = expenseRepository.save(expense);
        projectFinancialSummaryService.refreshExpenseAggregates(
                saved.getProject() != null ? saved.getProject().getId() : null,
                null,
                saved.getBudgetItem() != null ? saved.getBudgetItem().getId() : null,
                null
        );
        return expenseMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ExpenseResponseDTO> listAllExpenses(int page, int size, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> pageResult = projectId == null
                ? expenseRepository.findByIsActiveTrue(pageable)
                : expenseRepository.findByIsActiveTrueAndProject_Id(projectId, pageable);
        List<ExpenseResponseDTO> content = pageResult.getContent().stream()
                .map(expenseMapper::toDTO)
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
    public ExpenseResponseDTO findExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa nao encontrada"));
        if (!Boolean.TRUE.equals(expense.getIsActive())) {
            throw new ResourceNotFoundException("Despesa nao encontrada");
        }
        return expenseMapper.toDTO(expense);
    }

    @Override
    public ExpenseResponseDTO updateExpenseById(Long id, ExpenseUpdateDTO dto) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa nao encontrada"));
        if (!Boolean.TRUE.equals(expense.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma despesa inativa");
        }
        Long previousProjectId = expense.getProject() != null ? expense.getProject().getId() : null;
        Long previousBudgetItemId = expense.getBudgetItem() != null ? expense.getBudgetItem().getId() : null;
        expenseMapper.updateEntityFromDTO(dto, expense);
        applyReferencesOnUpdate(expense, dto);
        Expense updated = expenseRepository.save(expense);
        projectFinancialSummaryService.refreshExpenseAggregates(
                updated.getProject() != null ? updated.getProject().getId() : null,
                previousProjectId,
                updated.getBudgetItem() != null ? updated.getBudgetItem().getId() : null,
                previousBudgetItemId
        );
        return expenseMapper.toDTO(updated);
    }

    @Override
    @Transactional
    public void deleteExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa nao encontrada"));
        if (!Boolean.TRUE.equals(expense.getIsActive())) {
            throw new BusinessException("Despesa ja esta inativa");
        }
        Long projectId = expense.getProject() != null ? expense.getProject().getId() : null;
        Long budgetItemId = expense.getBudgetItem() != null ? expense.getBudgetItem().getId() : null;
        expense.setIsActive(false);
        expenseRepository.save(expense);
        projectFinancialSummaryService.refreshExpenseAggregates(projectId, null, budgetItemId, null);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO restoreExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa nao encontrada"));
        if (Boolean.TRUE.equals(expense.getIsActive())) {
            throw new BusinessException("Despesa ja esta ativa");
        }
        expense.setIsActive(true);
        Expense restored = expenseRepository.save(expense);
        projectFinancialSummaryService.refreshExpenseAggregates(
                restored.getProject() != null ? restored.getProject().getId() : null,
                null,
                restored.getBudgetItem() != null ? restored.getBudgetItem().getId() : null,
                null
        );
        return expenseMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }

    private void applyReferencesOnCreate(Expense expense, ExpenseRequestDTO dto) {
        expense.setProject(projectRepository.getReferenceById(requireProjectId(dto.projectId(), dto.incomeId())));
        expense.setBudgetItem(budgetItemRepository.getReferenceById(dto.budgetItemId()));
        expense.setCategory(budgetCategoryRepository.getReferenceById(dto.categoryId()));
        expense.setIncome(null);
        applyPaymentLink(expense, dto.personId(), dto.organizationId());
        expense.setDocument(dto.documentId() != null ? documentRepository.getReferenceById(dto.documentId()) : null);
    }

    private void applyReferencesOnUpdate(Expense expense, ExpenseUpdateDTO dto) {
        Long resolvedProjectId = resolveProjectId(dto.projectId(), dto.incomeId());
        if (resolvedProjectId != null) {
            expense.setProject(projectRepository.getReferenceById(resolvedProjectId));
            expense.setIncome(null);
        }
        if (dto.budgetItemId() != null) {
            expense.setBudgetItem(budgetItemRepository.getReferenceById(dto.budgetItemId()));
        }
        if (dto.categoryId() != null) {
            expense.setCategory(budgetCategoryRepository.getReferenceById(dto.categoryId()));
        }
        applyPaymentLink(expense, dto.personId(), dto.organizationId());
        if (dto.documentId() != null) {
            expense.setDocument(documentRepository.getReferenceById(dto.documentId()));
        }
    }

    private void applyPaymentLink(Expense expense, Long personId, Long organizationId) {
        if (personId != null) {
            expense.setPerson(peopleRepository.getReferenceById(personId));
            expense.setOrganization(null);
            return;
        }
        if (organizationId != null) {
            expense.setOrganization(organizationRepository.getReferenceById(organizationId));
            expense.setPerson(null);
            return;
        }
        expense.setPerson(null);
        expense.setOrganization(null);
    }

    private Long resolveProjectId(Long projectId, Long incomeId) {
        if (projectId != null) {
            return projectId;
        }
        if (incomeId != null) {
            return incomeRepository.findProjectIdById(incomeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Receita nao encontrada"));
        }
        return null;
    }

    private Long requireProjectId(Long projectId, Long incomeId) {
        Long resolvedProjectId = resolveProjectId(projectId, incomeId);
        if (resolvedProjectId == null) {
            throw new BusinessException("Projeto da despesa e obrigatorio");
        }
        return resolvedProjectId;
    }
}
