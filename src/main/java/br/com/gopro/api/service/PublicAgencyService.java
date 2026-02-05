package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyRequestDTO;
import br.com.gopro.api.dtos.PublicAgencyResponseDTO;
import br.com.gopro.api.dtos.PublicAgencyUpdateDTO;

public interface PublicAgencyService {
    PublicAgencyResponseDTO createPublicAgency(PublicAgencyRequestDTO dto);
    PageResponseDTO<PublicAgencyResponseDTO> listAllPublicAgencies(int page, int size);
    PublicAgencyResponseDTO findPublicAgencyById(Long id);
    PublicAgencyResponseDTO updatePublicAgencyById(Long id, PublicAgencyUpdateDTO dto);
    void deletePublicAgencyById(Long id);
    PublicAgencyResponseDTO restorePublicAgencyById(Long id);
}
