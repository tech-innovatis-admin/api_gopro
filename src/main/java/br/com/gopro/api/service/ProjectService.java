package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectDashboardResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectTotalsDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;

public interface ProjectService {
    ProjectResponseDTO createProject(ProjectRequestDTO dto);
    PageResponseDTO<ProjectResponseDTO> listAllProjects(int page, int size);
    ProjectResponseDTO findProjectById(Long id);
    ProjectResponseDTO updateProjectById(Long id, ProjectUpdateDTO dto);
    void deleteProjectById(Long id);
    ProjectResponseDTO restoreProjectById(Long id);
    ProjectTotalsDTO getProjectTotals(Long projectId);
    ProjectDashboardResponseDTO getDashboard(
            ProjectStatusEnum projectStatus,
            ProjectTypeEnum projectType,
            ProjectGovIfEnum projectGovIf,
            Boolean executedByInnovatis,
            Integer month,
            Integer year,
            String location,
            Long partnerId
    );
}
