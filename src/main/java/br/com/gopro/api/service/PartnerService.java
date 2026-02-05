package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PartnerRequestDTO;
import br.com.gopro.api.dtos.PartnerResponseDTO;
import br.com.gopro.api.dtos.PartnerUpdateDTO;

import java.util.List;

public interface PartnerService {
    PartnerResponseDTO createPartner(PartnerRequestDTO dto);
    PageResponseDTO<PartnerResponseDTO> listAllPartners(int page, int size);
    PartnerResponseDTO findPartnerById(Long id);
    PartnerResponseDTO updatePartnerById(Long id, PartnerUpdateDTO dto);
    void deletePartnerById(Long id);
    PartnerResponseDTO restorePartnerById(Long id);
}
