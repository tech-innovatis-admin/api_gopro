package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetTransferRequestDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;
import br.com.gopro.api.dtos.BudgetTransferUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface BudgetTransferService {
    BudgetTransferResponseDTO createBudgetTransfer(BudgetTransferRequestDTO dto);
    PageResponseDTO<BudgetTransferResponseDTO> listAllBudgetTransfers(int page, int size);
    BudgetTransferResponseDTO findBudgetTransferById(Long id);
    BudgetTransferResponseDTO updateBudgetTransferById(Long id, BudgetTransferUpdateDTO dto);
    void deleteBudgetTransferById(Long id);
    BudgetTransferResponseDTO restoreBudgetTransferById(Long id);
}