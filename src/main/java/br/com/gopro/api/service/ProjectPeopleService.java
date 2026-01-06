package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;

import java.util.List;

public interface ProjectPeopleService {

    ProjectPeopleResponseDTO createProjectPeople(ProjectPeopleRequestDTO dto);
    List<ProjectPeopleResponseDTO> listAllProjectPeople();
    ProjectPeopleResponseDTO findProjectPeopleById(Long id);
    ProjectPeopleResponseDTO updateProjectPeople(Long id, ProjectPeopleRequestDTO dto);
    void deleteProjectPeopleById(Long id);
}
