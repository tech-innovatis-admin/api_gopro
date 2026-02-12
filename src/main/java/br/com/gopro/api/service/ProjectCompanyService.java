package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyDetailedResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyRequestDTO;
import br.com.gopro.api.dtos.ProjectCompanyResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyUpdateDTO;

public interface ProjectCompanyService {
    ProjectCompanyResponseDTO createProjectCompany(ProjectCompanyRequestDTO dto);
    PageResponseDTO<ProjectCompanyResponseDTO> listAllProjectCompanies(int page, int size);
    PageResponseDTO<ProjectCompanyResponseDTO> listProjectCompaniesByProjectId(Long projectId, int page, int size);
    PageResponseDTO<ProjectCompanyDetailedResponseDTO> listProjectCompaniesDetailed(Long projectId, int page, int size);
    ProjectCompanyResponseDTO findProjectCompanyById(Long id);
    ProjectCompanyResponseDTO updateProjectCompanyById(Long id, ProjectCompanyUpdateDTO dto);
    void deleteProjectCompanyById(Long id);
    ProjectCompanyResponseDTO restoreProjectCompanyById(Long id);
}
