package br.com.gopro.api.service;

import br.com.gopro.api.dtos.GoalRequestDTO;
import br.com.gopro.api.dtos.GoalResponseDTO;
import br.com.gopro.api.dtos.GoalUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;

public interface GoalService {
    GoalResponseDTO createGoal(GoalRequestDTO dto);
    PageResponseDTO<GoalResponseDTO> listAllGoals(int page, int size, Long projectId);
    GoalResponseDTO findGoalById(Long id);
    GoalResponseDTO updateGoalById(Long id, GoalUpdateDTO dto);
    void deleteGoalById(Long id);
    GoalResponseDTO restoreGoalById(Long id);
    GoalResponseDTO concludeGoalById(Long id);
    GoalResponseDTO reopenGoalById(Long id);
}
