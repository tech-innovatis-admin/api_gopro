package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectOrganizationRequestDTO;
import br.com.gopro.api.dtos.ProjectOrganizationResponseDTO;
import br.com.gopro.api.mapper.ProjectOrganizationMapper;
import br.com.gopro.api.model.Organization;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectOrganization;
import br.com.gopro.api.repository.OrganizationRepository;
import br.com.gopro.api.repository.ProjectOrganizationRepository;
import br.com.gopro.api.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectOrganizationServiceImpl implements ProjectOrganizationService{

    private final ProjectOrganizationRepository projectOrganizationRepository;
    private final ProjectOrganizationMapper projectOrganizationMapper;
    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;


    @Override
    public ProjectOrganizationResponseDTO createProjectOrganization(ProjectOrganizationRequestDTO dto) {
        ProjectOrganization projectOrganization = projectOrganizationMapper.toEntity(dto);

        projectOrganization.setProject(findProjectById(dto.project()));
        projectOrganization.setOrganization(findOrganizationById(dto.organization()));

        ProjectOrganization projectOrganizationSaved = projectOrganizationRepository.save(projectOrganization);

        return projectOrganizationMapper.toDTO(projectOrganizationSaved);
    }

    @Override
    public List<ProjectOrganizationResponseDTO> listAllProjectOrganization() {
        return projectOrganizationRepository.findAll().stream()
                .map(projectOrganizationMapper::toDTO)
                .toList();
    }

    @Override
    public ProjectOrganizationResponseDTO findProjectOrganizationById(Long id) {
        ProjectOrganization projectOrganization = projectOrganizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base de dados"));

        return projectOrganizationMapper.toDTO(projectOrganization);
    }

    @Transactional
    @Override
    public ProjectOrganizationResponseDTO updateProjectOrganizationById(Long id, ProjectOrganizationRequestDTO dto) {
        ProjectOrganization projectOrganization = projectOrganizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base de dados"));

        projectOrganization.setProject(findProjectById(dto.project()));
        projectOrganization.setOrganization(findOrganizationById(dto.organization()));
        projectOrganization.setContractNumber(dto.contractNumber());
        projectOrganization.setDescription(dto.description());
        projectOrganization.setStartDate(dto.startDate());
        projectOrganization.setEndDate(dto.endDate());
        projectOrganization.setStatusProjectOrganization(dto.statusProjectOrganization());
        projectOrganization.setTotalValue(dto.totalValue());
        projectOrganization.setNotes(dto.notes());

        ProjectOrganization projectOrganizationUpdated = projectOrganizationRepository.save(projectOrganization);

        return projectOrganizationMapper.toDTO(projectOrganizationUpdated);
    }

    @Transactional
    @Override
    public void deleteProjectOrganizationById(Long id) {
        if (!projectOrganizationRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Projeto não encontrado na base de dados");
        }

        projectOrganizationRepository.deleteById(id);
    }

    private Project findProjectById(Long id){
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Projeto não encontrado na base de dados"));
    }

    private Organization findOrganizationById(Long id){
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Organização não encontrada na base de dados"));
    }
}
