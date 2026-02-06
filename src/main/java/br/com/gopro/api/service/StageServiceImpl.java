package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.StageRequestDTO;
import br.com.gopro.api.dtos.StageResponseDTO;
import br.com.gopro.api.dtos.StageUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.StageMapper;
import br.com.gopro.api.model.Stage;
import br.com.gopro.api.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {

    private final StageRepository stageRepository;
    private final StageMapper stageMapper;

    @Override
    public StageResponseDTO createStage(StageRequestDTO dto) {
        Stage stage = stageMapper.toEntity(dto);
        stage.setIsActive(true);
        Stage saved = stageRepository.save(stage);
        return stageMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<StageResponseDTO> listAllStages(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Stage> pageResult = stageRepository.findByIsActiveTrue(pageable);
        List<StageResponseDTO> content = pageResult.getContent().stream()
                .map(stageMapper::toDTO)
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
    public StageResponseDTO findStageById(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa nao encontrada"));
        if (!Boolean.TRUE.equals(stage.getIsActive())) {
            throw new ResourceNotFoundException("Etapa nao encontrada");
        }
        return stageMapper.toDTO(stage);
    }

    @Override
    public StageResponseDTO updateStageById(Long id, StageUpdateDTO dto) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa nao encontrada"));
        if (!Boolean.TRUE.equals(stage.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma etapa inativa");
        }
        stageMapper.updateEntityFromDTO(dto, stage);
        Stage updated = stageRepository.save(stage);
        return stageMapper.toDTO(updated);
    }

    @Override
    public void deleteStageById(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa nao encontrada"));
        if (!Boolean.TRUE.equals(stage.getIsActive())) {
            throw new BusinessException("Etapa ja esta inativa");
        }
        stage.setIsActive(false);
        stageRepository.save(stage);
    }

    @Override
    public StageResponseDTO restoreStageById(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa nao encontrada"));
        if (Boolean.TRUE.equals(stage.getIsActive())) {
            throw new BusinessException("Etapa ja esta ativa");
        }
        stage.setIsActive(true);
        Stage restored = stageRepository.save(stage);
        return stageMapper.toDTO(restored);
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