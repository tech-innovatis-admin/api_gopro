package br.com.gopro.api.service;

import br.com.gopro.api.dtos.OrganizationRequestDTO;
import br.com.gopro.api.dtos.OrganizationResponseDTO;

import java.util.List;

public interface OrganizationService {

    OrganizationRequestDTO createOrganization(OrganizationRequestDTO dto);
    List<OrganizationResponseDTO> listAllOrganization();
    OrganizationResponseDTO listOrganizationById(Long id);
    OrganizationResponseDTO updateOrganization(Long id, OrganizationRequestDTO dto);
    void deleteOrganization(Long id);
}
