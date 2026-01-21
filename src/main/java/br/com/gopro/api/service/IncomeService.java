package br.com.gopro.api.service;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;

import java.util.List;

public interface IncomeService {
    IncomeResponseDTO createIncome(IncomeRequestDTO dto);
    List<IncomeResponseDTO> listAllIncome();
    IncomeResponseDTO findIncomeById(Long id);
    IncomeResponseDTO updatedIncomeById(Long id, IncomeRequestDTO dto);
    void deleteIncomeById(Long id);
}
