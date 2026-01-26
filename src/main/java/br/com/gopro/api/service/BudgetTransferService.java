package br.com.gopro.api.service;

import br.com.gopro.api.dtos.BudgetTransferDTO;
import br.com.gopro.api.dtos.BudgetTransferResponseDTO;

import java.util.List;

public interface BudgetTransferService {
    BudgetTransferResponseDTO createBudgetTransfer(BudgetTransferDTO dto);
    List<BudgetTransferResponseDTO> listAllBudgetTransfers();
    BudgetTransferResponseDTO findBudgetTransferById(Long id);
    BudgetTransferResponseDTO updateTransferBudgetById(Long id, BudgetTransferDTO dto);
    void deleteTransferBudgetById(Long id);
}
