package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.mapper.ProjectMapper;
import br.com.gopro.api.model.Organization;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.OrganizationRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService{

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        Project project = projectMapper.toEntity(dto);

        project.setOrgaoFinancioador(findOrganization(dto.orgaoFinanciador()));
        project.setExecutingOrg(findOrganization(dto.executionOrg()));

        projectRepository.save(project);

        return projectMapper.toDTO(project);
    }

    @Override
    public List<ProjectResponseDTO> listAllProducts() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toDTO)
                .toList();
    }

    @Override
    public ProjectResponseDTO listProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado"));

        return projectMapper.toDTO(project);
    }

    @Transactional
    @Override
    public ProjectResponseDTO updateProject(Long id, ProjectRequestDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não econtrado"));

        project.setName(dto.name());
        project.setCode(dto.code());
        project.setStatusProjects(dto.statusProjects());
        project.setAreaSegmento(dto.areaSegmento());
        project.setOrgaoFinancioador(findOrganization(dto.orgaoFinanciador()));
        project.setExecutingOrg(findOrganization(dto.executionOrg()));
        project.setCordinator(dto.cordinator());
        project.setScope(dto.scope());
        project.setContractValue(dto.contractValue());
        project.setStartDate(dto.startDate());
        project.setEndDate(dto.endDate());
        project.setOpeningDate(dto.openingDate());
        project.setExecutionLocation(dto.executionLocation());
        project.setCreatedBy(dto.createdBy());

        projectRepository.save(project);

        return projectMapper.toDTO(project);
    }

    @Transactional
    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Projeto não encontrado!");
        }

        projectRepository.deleteById(id);
    }


    private Organization findOrganization(Long id){
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organização não encontrada"));
    }

}
