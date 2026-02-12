package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyDetailedResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyRequestDTO;
import br.com.gopro.api.dtos.ProjectCompanyResponseDTO;
import br.com.gopro.api.dtos.ProjectCompanyUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ProjectCompanyMapper;
import br.com.gopro.api.model.ProjectCompany;
import br.com.gopro.api.repository.ProjectCompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectCompanyServiceImpl implements ProjectCompanyService {

    private final ProjectCompanyRepository projectCompanyRepository;
    private final ProjectCompanyMapper projectCompanyMapper;

    @Override
    public ProjectCompanyResponseDTO createProjectCompany(ProjectCompanyRequestDTO dto) {
        ProjectCompany projectCompany = projectCompanyMapper.toEntity(dto);
        if (isBlank(projectCompany.getContractNumber())) {
            projectCompany.setContractNumber(generateContractNumber());
        }
        projectCompany.setIsActive(true);
        ProjectCompany saved = projectCompanyRepository.save(projectCompany);
        return projectCompanyMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ProjectCompanyResponseDTO> listAllProjectCompanies(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectCompany> pageResult = projectCompanyRepository.findByIsActiveTrue(pageable);
        return toPageResponse(pageResult);
    }

    @Override
    public PageResponseDTO<ProjectCompanyResponseDTO> listProjectCompaniesByProjectId(Long projectId, int page, int size) {
        validatePage(page, size);
        validateProjectId(projectId);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectCompany> pageResult = projectCompanyRepository.findByProject_IdAndIsActiveTrue(projectId, pageable);
        return toPageResponse(pageResult);
    }

    @Override
    public PageResponseDTO<ProjectCompanyDetailedResponseDTO> listProjectCompaniesDetailed(Long projectId, int page, int size) {
        validatePage(page, size);
        if (projectId != null) {
            validateProjectId(projectId);
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectCompanyDetailedResponseDTO> pageResult =
                projectCompanyRepository.findDetailedByProjectId(projectId, pageable);

        return new PageResponseDTO<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public ProjectCompanyResponseDTO findProjectCompanyById(Long id) {
        ProjectCompany projectCompany = projectCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-empresa nao encontrado"));
        if (!Boolean.TRUE.equals(projectCompany.getIsActive())) {
            throw new ResourceNotFoundException("Vinculo projeto-empresa nao encontrado");
        }
        return projectCompanyMapper.toDTO(projectCompany);
    }

    @Override
    public ProjectCompanyResponseDTO updateProjectCompanyById(Long id, ProjectCompanyUpdateDTO dto) {
        ProjectCompany projectCompany = projectCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-empresa nao encontrado"));
        if (!Boolean.TRUE.equals(projectCompany.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um vinculo inativo");
        }
        projectCompanyMapper.updateEntityFromDTO(dto, projectCompany);
        if (isBlank(projectCompany.getContractNumber())) {
            projectCompany.setContractNumber(generateContractNumber());
        }
        ProjectCompany updated = projectCompanyRepository.save(projectCompany);
        return projectCompanyMapper.toDTO(updated);
    }

    @Override
    public void deleteProjectCompanyById(Long id) {
        ProjectCompany projectCompany = projectCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-empresa nao encontrado"));
        if (!Boolean.TRUE.equals(projectCompany.getIsActive())) {
            throw new BusinessException("Vinculo projeto-empresa ja esta inativo");
        }
        projectCompany.setIsActive(false);
        projectCompanyRepository.save(projectCompany);
    }

    @Override
    public ProjectCompanyResponseDTO restoreProjectCompanyById(Long id) {
        ProjectCompany projectCompany = projectCompanyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-empresa nao encontrado"));
        if (Boolean.TRUE.equals(projectCompany.getIsActive())) {
            throw new BusinessException("Vinculo projeto-empresa ja esta ativo");
        }
        projectCompany.setIsActive(true);
        ProjectCompany restored = projectCompanyRepository.save(projectCompany);
        return projectCompanyMapper.toDTO(restored);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }

    private void validateProjectId(Long projectId) {
        if (projectId == null || projectId <= 0) {
            throw new BusinessException("ProjectId invalido");
        }
    }

    private PageResponseDTO<ProjectCompanyResponseDTO> toPageResponse(Page<ProjectCompany> pageResult) {
        List<ProjectCompanyResponseDTO> content = pageResult.getContent().stream()
                .map(projectCompanyMapper::toDTO)
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

    private String generateContractNumber() {
        Long sequence = projectCompanyRepository.nextContractNumberSequence();
        int year = Year.now().getValue();
        return String.format("CT-%d-%06d", year, sequence);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
