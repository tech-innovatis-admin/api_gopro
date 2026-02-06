package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;
import br.com.gopro.api.dtos.ExpenseUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface ExpenseService {
    ExpenseResponseDTO createExpense(ExpenseRequestDTO dto);
    PageResponseDTO<ExpenseResponseDTO> listAllExpenses(int page, int size);
    ExpenseResponseDTO findExpenseById(Long id);
    ExpenseResponseDTO updateExpenseById(Long id, ExpenseUpdateDTO dto);
    void deleteExpenseById(Long id);
    ExpenseResponseDTO restoreExpenseById(Long id);
}