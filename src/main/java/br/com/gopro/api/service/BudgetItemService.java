package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetItemRequestDTO;
import br.com.gopro.api.dtos.BudgetItemResponseDTO;

import java.util.List;

public interface BudgetItemService {

    BudgetItemResponseDTO createBudgetItem(BudgetItemRequestDTO dto);

    List<BudgetItemResponseDTO> listAllBudgetItems();

    BudgetItemResponseDTO findBudgetItemById(Long id);

    BudgetItemResponseDTO updateBudgetItemById(Long id, BudgetItemRequestDTO dto);

    void deleteBudgetItemById(Long id);
}
