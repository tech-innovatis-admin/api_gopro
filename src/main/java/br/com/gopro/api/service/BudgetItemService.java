package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;
import br.com.gopro.api.dtos.BudgetItemUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface BudgetItemService {
    BudgetItemResponseDTO createBudgetItem(BudgetItemRequestDTO dto);
    PageResponseDTO<BudgetItemResponseDTO> listAllBudgetItems(int page, int size);
    BudgetItemResponseDTO findBudgetItemById(Long id);
    BudgetItemResponseDTO updateBudgetItemById(Long id, BudgetItemUpdateDTO dto);
    void deleteBudgetItemById(Long id);
    BudgetItemResponseDTO restoreBudgetItemById(Long id);
}