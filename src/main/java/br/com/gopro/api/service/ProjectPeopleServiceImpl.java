package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.mapper.ProjectPeopleMapper;
import br.com.gopro.api.model.People;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectPeopleServiceImpl implements ProjectPeopleService{

    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectPeopleMapper projectPeopleMapper;
    private final ProjectRepository projectRepository;
    private final PeopleRepository peopleRepository;

    @Override
    public ProjectPeopleResponseDTO createProjectPeople(ProjectPeopleRequestDTO dto) {
        ProjectPeople projectPeople = projectPeopleMapper.toEntity(dto);

        projectPeople.setProject(findProjectById(dto.project()));
        projectPeople.setPeople(findPeopleById(dto.people()));

        ProjectPeople projectPeopleSaved = projectPeopleRepository.save(projectPeople);

        return projectPeopleMapper.toDTO(projectPeopleSaved);
    }

    @Override
    public List<ProjectPeopleResponseDTO> listAllProjectPeople() {
        return projectPeopleRepository.findAll().stream()
                .map(projectPeopleMapper::toDTO)
                .toList();
    }

    @Override
    public ProjectPeopleResponseDTO findProjectPeopleById(Long id) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base de dados"));

        return projectPeopleMapper.toDTO(projectPeople);
    }

    @Transactional
    @Override
    public ProjectPeopleResponseDTO updateProjectPeople(Long id, ProjectPeopleRequestDTO dto) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base de dados"));

        projectPeople.setProject(findProjectById(dto.project()));
        projectPeople.setPeople(findPeopleById(dto.people()));
        projectPeople.setRoleProjectPeople(dto.roleProjectPeople());
        projectPeople.setStartDate(dto.starteDate());
        projectPeople.setStatusProjectPeople(dto.statusProjectPeople());
        projectPeople.setBaseAmount(dto.baseAmount());
        projectPeople.setNotes(dto.notes());
        projectPeople.setCreatedBy(dto.createdBy());

        ProjectPeople projectPeopleUpdated = projectPeopleRepository.save(projectPeople);

        return projectPeopleMapper.toDTO(projectPeopleUpdated);
    }

    @Transactional
    @Override
    public void deleteProjectPeopleById(Long id) {
        if (!projectPeopleRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Projeto não encontrado na base!");
        }

        projectPeopleRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base"));
    }

    private People findPeopleById(Long id){
        return peopleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Pessoa não encontrada na base"));
    }
}
