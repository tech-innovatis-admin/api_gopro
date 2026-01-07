package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectOrganizationRequestDTO;
import br.com.gopro.api.dtos.ProjectOrganizationResponseDTO;

import java.util.List;

public interface ProjectOrganizationService {

    ProjectOrganizationResponseDTO createProjectOrganization(ProjectOrganizationRequestDTO dto);
    List<ProjectOrganizationResponseDTO> listAllProjectOrganization();
    ProjectOrganizationResponseDTO findProjectOrganizationById(Long id);
    ProjectOrganizationResponseDTO updateProjectOrganizationById(Long id, ProjectOrganizationRequestDTO dto);
    void deleteProjectOrganizationById(Long id);
}
