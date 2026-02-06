package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PhaseRequestDTO;
import br.com.gopro.api.dtos.PhaseResponseDTO;
import br.com.gopro.api.dtos.PhaseUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.PhaseMapper;
import br.com.gopro.api.model.Phase;
import br.com.gopro.api.repository.PhaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PhaseServiceImpl implements PhaseService {

    private final PhaseRepository phaseRepository;
    private final PhaseMapper phaseMapper;

    @Override
    public PhaseResponseDTO createPhase(PhaseRequestDTO dto) {
        Phase phase = phaseMapper.toEntity(dto);
        phase.setIsActive(true);
        Phase saved = phaseRepository.save(phase);
        return phaseMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<PhaseResponseDTO> listAllPhases(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Phase> pageResult = phaseRepository.findByIsActiveTrue(pageable);
        List<PhaseResponseDTO> content = pageResult.getContent().stream()
                .map(phaseMapper::toDTO)
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
    public PhaseResponseDTO findPhaseById(Long id) {
        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fase nao encontrada"));
        if (!Boolean.TRUE.equals(phase.getIsActive())) {
            throw new ResourceNotFoundException("Fase nao encontrada");
        }
        return phaseMapper.toDTO(phase);
    }

    @Override
    public PhaseResponseDTO updatePhaseById(Long id, PhaseUpdateDTO dto) {
        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fase nao encontrada"));
        if (!Boolean.TRUE.equals(phase.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma fase inativa");
        }
        phaseMapper.updateEntityFromDTO(dto, phase);
        Phase updated = phaseRepository.save(phase);
        return phaseMapper.toDTO(updated);
    }

    @Override
    public void deletePhaseById(Long id) {
        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fase nao encontrada"));
        if (!Boolean.TRUE.equals(phase.getIsActive())) {
            throw new BusinessException("Fase ja esta inativa");
        }
        phase.setIsActive(false);
        phaseRepository.save(phase);
    }

    @Override
    public PhaseResponseDTO restorePhaseById(Long id) {
        Phase phase = phaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fase nao encontrada"));
        if (Boolean.TRUE.equals(phase.getIsActive())) {
            throw new BusinessException("Fase ja esta ativa");
        }
        phase.setIsActive(true);
        Phase restored = phaseRepository.save(phase);
        return phaseMapper.toDTO(restored);
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