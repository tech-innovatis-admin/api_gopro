package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleDetailedResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleUpdateDTO;

public interface ProjectPeopleService {
    ProjectPeopleResponseDTO createProjectPeople(ProjectPeopleRequestDTO dto);
    PageResponseDTO<ProjectPeopleResponseDTO> listAllProjectPeople(int page, int size);
    PageResponseDTO<ProjectPeopleResponseDTO> listProjectPeopleByProjectId(Long projectId, int page, int size);
    PageResponseDTO<ProjectPeopleDetailedResponseDTO> listProjectPeopleDetailed(Long projectId, int page, int size);
    ProjectPeopleResponseDTO findProjectPeopleById(Long id);
    ProjectPeopleResponseDTO updateProjectPeopleById(Long id, ProjectPeopleUpdateDTO dto);
    void deleteProjectPeopleById(Long id);
    ProjectPeopleResponseDTO restoreProjectPeopleById(Long id);
}
