package br.com.gopro.api.service;

import br.com.gopro.api.dtos.IncomeRequestDTO;
import br.com.gopro.api.dtos.IncomeResponseDTO;
import br.com.gopro.api.dtos.IncomeUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface IncomeService {
    IncomeResponseDTO createIncome(IncomeRequestDTO dto);
    PageResponseDTO<IncomeResponseDTO> listAllIncomes(int page, int size);
    IncomeResponseDTO findIncomeById(Long id);
    IncomeResponseDTO updateIncomeById(Long id, IncomeUpdateDTO dto);
    void deleteIncomeById(Long id);
    IncomeResponseDTO restoreIncomeById(Long id);
}