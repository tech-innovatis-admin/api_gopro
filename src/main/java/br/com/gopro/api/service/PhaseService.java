package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.PhaseRequestDTO;
import br.com.gopro.api.dtos.PhaseResponseDTO;
import br.com.gopro.api.dtos.PhaseUpdateDTO;

public interface PhaseService {
    PhaseResponseDTO createPhase(PhaseRequestDTO dto);
    PageResponseDTO<PhaseResponseDTO> listAllPhases(int page, int size, Long stageId, Long projectId);
    PhaseResponseDTO findPhaseById(Long id);
    PhaseResponseDTO updatePhaseById(Long id, PhaseUpdateDTO dto);
    void deletePhaseById(Long id);
    PhaseResponseDTO restorePhaseById(Long id);
    PhaseResponseDTO concludePhaseById(Long id);
    PhaseResponseDTO reopenPhaseById(Long id);
}
