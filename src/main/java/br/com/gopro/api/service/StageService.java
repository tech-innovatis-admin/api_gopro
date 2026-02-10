package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.StageRequestDTO;
import br.com.gopro.api.dtos.StageResponseDTO;
import br.com.gopro.api.dtos.StageUpdateDTO;

public interface StageService {
    StageResponseDTO createStage(StageRequestDTO dto);
    PageResponseDTO<StageResponseDTO> listAllStages(int page, int size, Long goalId, Long projectId);
    StageResponseDTO findStageById(Long id);
    StageResponseDTO updateStageById(Long id, StageUpdateDTO dto);
    void deleteStageById(Long id);
    StageResponseDTO restoreStageById(Long id);
    StageResponseDTO concludeStageById(Long id);
    StageResponseDTO reopenStageById(Long id);
}
