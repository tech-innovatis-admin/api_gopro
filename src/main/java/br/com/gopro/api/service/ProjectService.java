package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;

import java.util.List;

public interface ProjectService {

    ProjectResponseDTO createProjec(ProjectRequestDTO dto);
    List<ProjectResponseDTO> listAllProducts();
    ProjectResponseDTO listProjectById(Long id);
    ProjectResponseDTO updateProject(Long id, ProjectRequestDTO dto);
    ProjectResponseDTO deleteProject(Long id);
}
