package br.com.gopro.api.service;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;
import br.com.gopro.api.dtos.OrganizationUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface OrganizationService {
    OrganizationResponseDTO createOrganization(OrganizationRequestDTO dto);
    PageResponseDTO<OrganizationResponseDTO> listAllOrganizations(int page, int size);
    OrganizationResponseDTO findOrganizationById(Long id);
    OrganizationResponseDTO updateOrganizationById(Long id, OrganizationUpdateDTO dto);
    void deleteOrganizationById(Long id);
    OrganizationResponseDTO restoreOrganizationById(Long id);
}