package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ExpenseRequestDTO;
import br.com.gopro.api.dtos.ExpenseResponseDTO;

import java.util.List;

public interface ExpenseService {
    ExpenseResponseDTO createExpense(ExpenseRequestDTO dto);
    List<ExpenseResponseDTO> listAllExpenses();
    ExpenseResponseDTO findExpenseById(Long id);
    ExpenseResponseDTO updatedExpenseById(Long id, ExpenseRequestDTO dto);
    void deleteExpenseById(Long id);
}
