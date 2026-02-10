package br.com.gopro.api.service;

import br.com.gopro.api.dtos.GoalRequestDTO;
import br.com.gopro.api.dtos.GoalResponseDTO;
import br.com.gopro.api.dtos.GoalUpdateDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.GoalMapper;
import br.com.gopro.api.model.Goal;
import br.com.gopro.api.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final GoalMapper goalMapper;

    @Override
    public GoalResponseDTO createGoal(GoalRequestDTO dto) {
        Goal goal = goalMapper.toEntity(dto);
        goal.setIsActive(true);
        Goal saved = goalRepository.save(goal);
        return goalMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<GoalResponseDTO> listAllGoals(int page, int size, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Goal> pageResult = projectId == null
                ? goalRepository.findByIsActiveTrue(pageable)
                : goalRepository.findByIsActiveTrueAndProject_Id(projectId, pageable);
        List<GoalResponseDTO> content = pageResult.getContent().stream()
                .map(goalMapper::toDTO)
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
    public GoalResponseDTO findGoalById(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (!Boolean.TRUE.equals(goal.getIsActive())) {
            throw new ResourceNotFoundException("Meta nao encontrada");
        }
        return goalMapper.toDTO(goal);
    }

    @Override
    public GoalResponseDTO updateGoalById(Long id, GoalUpdateDTO dto) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (!Boolean.TRUE.equals(goal.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar uma meta inativa");
        }
        goalMapper.updateEntityFromDTO(dto, goal);
        Goal updated = goalRepository.save(goal);
        return goalMapper.toDTO(updated);
    }

    @Override
    public void deleteGoalById(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (!Boolean.TRUE.equals(goal.getIsActive())) {
            throw new BusinessException("Meta ja esta inativa");
        }
        goal.setIsActive(false);
        goalRepository.save(goal);
    }

    @Override
    public GoalResponseDTO restoreGoalById(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (Boolean.TRUE.equals(goal.getIsActive())) {
            throw new BusinessException("Meta ja esta ativa");
        }
        goal.setIsActive(true);
        Goal restored = goalRepository.save(goal);
        return goalMapper.toDTO(restored);
    }

    @Override
    public GoalResponseDTO concludeGoalById(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (!Boolean.TRUE.equals(goal.getIsActive())) {
            throw new BusinessException("Nao e possivel concluir uma meta inativa");
        }

        LocalDate today = LocalDate.now();
        goal.setDataConclusao(today);
        if (goal.getDataFim() == null) {
            goal.setDataFim(today);
        }

        Goal updated = goalRepository.save(goal);
        return goalMapper.toDTO(updated);
    }

    @Override
    public GoalResponseDTO reopenGoalById(Long id) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (!Boolean.TRUE.equals(goal.getIsActive())) {
            throw new BusinessException("Nao e possivel reabrir uma meta inativa");
        }

        goal.setDataConclusao(null);
        Goal updated = goalRepository.save(goal);
        return goalMapper.toDTO(updated);
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
