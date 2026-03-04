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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    @Override
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        applyReferencesOnCreate(expense, dto);
        expense.setIsActive(true);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ExpenseResponseDTO> listAllExpenses(int page, int size, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> pageResult = projectId == null
                ? expenseRepository.findByIsActiveTrue(pageable)
                : expenseRepository.findByIsActiveTrueAndIncome_IsActiveTrueAndIncome_Project_Id(projectId, pageable);
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
        expenseMapper.updateEntityFromDTO(dto, expense);
        applyReferencesOnUpdate(expense, dto);
        Expense updated = expenseRepository.save(expense);
        return expenseMapper.toDTO(updated);
    }

    @Override
    public void deleteExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa nao encontrada"));
        if (!Boolean.TRUE.equals(expense.getIsActive())) {
            throw new BusinessException("Despesa ja esta inativa");
        }
        expense.setIsActive(false);
        expenseRepository.save(expense);
    }

    @Override
    public ExpenseResponseDTO restoreExpenseById(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despesa nao encontrada"));
        if (Boolean.TRUE.equals(expense.getIsActive())) {
            throw new BusinessException("Despesa ja esta ativa");
        }
        expense.setIsActive(true);
        Expense restored = expenseRepository.save(expense);
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
        expense.setBudgetItem(budgetItemRepository.getReferenceById(dto.budgetItemId()));
        expense.setCategory(budgetCategoryRepository.getReferenceById(dto.categoryId()));
        expense.setIncome(incomeRepository.getReferenceById(dto.incomeId()));
        expense.setPerson(dto.personId() != null ? peopleRepository.getReferenceById(dto.personId()) : null);
        expense.setOrganization(
                dto.organizationId() != null ? organizationRepository.getReferenceById(dto.organizationId()) : null
        );
        expense.setDocument(dto.documentId() != null ? documentRepository.getReferenceById(dto.documentId()) : null);
    }

    private void applyReferencesOnUpdate(Expense expense, ExpenseUpdateDTO dto) {
        if (dto.budgetItemId() != null) {
            expense.setBudgetItem(budgetItemRepository.getReferenceById(dto.budgetItemId()));
        }
        if (dto.categoryId() != null) {
            expense.setCategory(budgetCategoryRepository.getReferenceById(dto.categoryId()));
        }
        if (dto.incomeId() != null) {
            expense.setIncome(incomeRepository.getReferenceById(dto.incomeId()));
        }
        if (dto.personId() != null) {
            expense.setPerson(peopleRepository.getReferenceById(dto.personId()));
        }
        if (dto.organizationId() != null) {
            expense.setOrganization(organizationRepository.getReferenceById(dto.organizationId()));
        }
        if (dto.documentId() != null) {
            expense.setDocument(documentRepository.getReferenceById(dto.documentId()));
        }
    }
}
