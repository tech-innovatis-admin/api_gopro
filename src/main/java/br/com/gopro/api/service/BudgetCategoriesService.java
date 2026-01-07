package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetCategoriesRequestDTO;
import br.com.gopro.api.dtos.BudgetCategoriesResponseDTO;

import java.util.List;

public interface BudgetCategoriesService {

    BudgetCategoriesResponseDTO createBudgetCategories(BudgetCategoriesRequestDTO dto);
    List<BudgetCategoriesResponseDTO> listAllBudgetCategories();
    BudgetCategoriesResponseDTO findBudgetCategorieById(Long id);
    BudgetCategoriesResponseDTO updatedBudgetCategoriesById(Long id, BudgetCategoriesRequestDTO dto);
    void deleteBudgetCategoriesById(Long id);
}
