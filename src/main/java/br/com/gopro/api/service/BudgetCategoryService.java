package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetCategoryRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoryResponseDTO;
import br.com.gopro.api.dtos.BudgetCategoryUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface BudgetCategoryService {
    BudgetCategoryResponseDTO createBudgetCategory(BudgetCategoryRequestDTO dto);
    PageResponseDTO<BudgetCategoryResponseDTO> listAllBudgetCategories(int page, int size, Long projectId);
    BudgetCategoryResponseDTO findBudgetCategoryById(Long id);
    BudgetCategoryResponseDTO updateBudgetCategoryById(Long id, BudgetCategoryUpdateDTO dto);
    void deleteBudgetCategoryById(Long id);
    BudgetCategoryResponseDTO restoreBudgetCategoryById(Long id);
}
