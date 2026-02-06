package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleRequestDTO;
import br.com.gopro.api.dtos.ProjectPeopleResponseDTO;
import br.com.gopro.api.dtos.ProjectPeopleUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ProjectPeopleMapper;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectPeopleServiceImpl implements ProjectPeopleService {

    private final ProjectPeopleRepository projectPeopleRepository;
    private final ProjectPeopleMapper projectPeopleMapper;

    @Override
    public ProjectPeopleResponseDTO createProjectPeople(ProjectPeopleRequestDTO dto) {
        ProjectPeople projectPeople = projectPeopleMapper.toEntity(dto);
        projectPeople.setIsActive(true);
        ProjectPeople saved = projectPeopleRepository.save(projectPeople);
        return projectPeopleMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ProjectPeopleResponseDTO> listAllProjectPeople(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<ProjectPeople> pageResult = projectPeopleRepository.findByIsActiveTrue(pageable);
        List<ProjectPeopleResponseDTO> content = pageResult.getContent().stream()
                .map(projectPeopleMapper::toDTO)
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
    public ProjectPeopleResponseDTO findProjectPeopleById(Long id) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-pessoa nao encontrado"));
        if (!Boolean.TRUE.equals(projectPeople.getIsActive())) {
            throw new ResourceNotFoundException("Vinculo projeto-pessoa nao encontrado");
        }
        return projectPeopleMapper.toDTO(projectPeople);
    }

    @Override
    public ProjectPeopleResponseDTO updateProjectPeopleById(Long id, ProjectPeopleUpdateDTO dto) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-pessoa nao encontrado"));
        if (!Boolean.TRUE.equals(projectPeople.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um vinculo inativo");
        }
        projectPeopleMapper.updateEntityFromDTO(dto, projectPeople);
        ProjectPeople updated = projectPeopleRepository.save(projectPeople);
        return projectPeopleMapper.toDTO(updated);
    }

    @Override
    public void deleteProjectPeopleById(Long id) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-pessoa nao encontrado"));
        if (!Boolean.TRUE.equals(projectPeople.getIsActive())) {
            throw new BusinessException("Vinculo projeto-pessoa ja esta inativo");
        }
        projectPeople.setIsActive(false);
        projectPeopleRepository.save(projectPeople);
    }

    @Override
    public ProjectPeopleResponseDTO restoreProjectPeopleById(Long id) {
        ProjectPeople projectPeople = projectPeopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vinculo projeto-pessoa nao encontrado"));
        if (Boolean.TRUE.equals(projectPeople.getIsActive())) {
            throw new BusinessException("Vinculo projeto-pessoa ja esta ativo");
        }
        projectPeople.setIsActive(true);
        ProjectPeople restored = projectPeopleRepository.save(projectPeople);
        return projectPeopleMapper.toDTO(restored);
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