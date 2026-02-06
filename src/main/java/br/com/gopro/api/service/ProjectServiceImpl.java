package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ProjectMapper;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        Project project = projectMapper.toEntity(dto);
        project.setIsActive(true);
        if (project.getTotalReceived() == null) {
            project.setTotalReceived(BigDecimal.ZERO);
        }
        if (project.getTotalExpenses() == null) {
            project.setTotalExpenses(BigDecimal.ZERO);
        }
        if (project.getSaldo() == null) {
            project.setSaldo(BigDecimal.ZERO);
        }
        Project saved = projectRepository.save(project);
        return projectMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ProjectResponseDTO> listAllProjects(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> pageResult = projectRepository.findByIsActiveTrue(pageable);
        List<ProjectResponseDTO> content = pageResult.getContent().stream()
                .map(projectMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public ProjectResponseDTO findProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new ResourceNotFoundException("Projeto nao encontrado");
        }
        return projectMapper.toDTO(project);
    }

    @Override
    public ProjectResponseDTO updateProjectById(Long id, ProjectUpdateDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um projeto inativo");
        }
        projectMapper.updateEntityFromDTO(dto, project);
        Project updated = projectRepository.save(project);
        return projectMapper.toDTO(updated);
    }

    @Override
    public void deleteProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new BusinessException("Projeto ja esta inativo");
        }
        project.setIsActive(false);
        projectRepository.save(project);
    }

    @Override
    public ProjectResponseDTO restoreProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (Boolean.TRUE.equals(project.getIsActive())) {
            throw new BusinessException("Projeto ja esta ativo");
        }
        project.setIsActive(true);
        Project restored = projectRepository.save(project);
        return projectMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }
}