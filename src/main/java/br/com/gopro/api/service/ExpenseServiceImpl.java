package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ExpenseMapper;
import br.com.gopro.api.model.Expense;
import br.com.gopro.api.repository.ExpenseRepository;
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

    @Override
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO dto) {
        Expense expense = expenseMapper.toEntity(dto);
        expense.setIsActive(true);
        Expense saved = expenseRepository.save(expense);
        return expenseMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ExpenseResponseDTO> listAllExpenses(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Expense> pageResult = expenseRepository.findByIsActiveTrue(pageable);
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
}