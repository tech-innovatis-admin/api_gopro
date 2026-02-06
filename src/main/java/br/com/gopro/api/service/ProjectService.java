package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;

public interface ProjectService {
    ProjectResponseDTO createProject(ProjectRequestDTO dto);
    PageResponseDTO<ProjectResponseDTO> listAllProjects(int page, int size);
    ProjectResponseDTO findProjectById(Long id);
    ProjectResponseDTO updateProjectById(Long id, ProjectUpdateDTO dto);
    void deleteProjectById(Long id);
    ProjectResponseDTO restoreProjectById(Long id);
}