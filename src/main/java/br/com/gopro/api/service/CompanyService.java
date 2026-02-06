package br.com.gopro.api.service;

import br.com.gopro.api.dtos.CompanyRequestDTO;
import br.com.gopro.api.dtos.CompanyResponseDTO;
import br.com.gopro.api.dtos.CompanyUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface CompanyService {
    CompanyResponseDTO createCompany(CompanyRequestDTO dto);
    PageResponseDTO<CompanyResponseDTO> listAllCompanies(int page, int size);
    CompanyResponseDTO findCompanyById(Long id);
    CompanyResponseDTO updateCompanyById(Long id, CompanyUpdateDTO dto);
    void deleteCompanyById(Long id);
    CompanyResponseDTO restoreCompanyById(Long id);
}