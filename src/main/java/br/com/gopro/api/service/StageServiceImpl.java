package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.StageRequestDTO;
import br.com.gopro.api.dtos.StageResponseDTO;
import br.com.gopro.api.dtos.StageUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.StageMapper;
import br.com.gopro.api.model.Goal;
import br.com.gopro.api.model.Stage;
import br.com.gopro.api.repository.GoalRepository;
import br.com.gopro.api.repository.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StageServiceImpl implements StageService {

    private final StageRepository stageRepository;
    private final GoalRepository goalRepository;
    private final StageMapper stageMapper;

    @Override
    public StageResponseDTO createStage(StageRequestDTO dto) {
        Stage stage = stageMapper.toEntity(dto);
        Goal goal = findActiveGoal(dto.goalId());
        stage.setGoal(goal);
        normalizeFinancialFields(stage, goal, null);
        stage.setIsActive(true);
        Stage saved = stageRepository.save(stage);
        return stageMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<StageResponseDTO> listAllStages(int page, int size, Long goalId, Long projectId) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Stage> pageResult;
        if (goalId != null) {
            pageResult = stageRepository.findByIsActiveTrueAndGoal_Id(goalId, pageable);
        } else if (projectId != null) {
            pageResult = stageRepository.findByIsActiveTrueAndGoal_Project_Id(projectId, pageable);
        } else {
            pageResult = stageRepository.findByIsActiveTrue(pageable);
        }
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
        Goal goal = findActiveGoal(resolveGoalId(stage, dto));
        stage.setGoal(goal);
        normalizeFinancialFields(stage, goal, id);
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

    @Override
    public StageResponseDTO concludeStageById(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa nao encontrada"));
        if (!Boolean.TRUE.equals(stage.getIsActive())) {
            throw new BusinessException("Nao e possivel concluir uma etapa inativa");
        }

        LocalDate today = LocalDate.now();
        stage.setDataConclusao(today);
        if (stage.getDataFim() == null) {
            stage.setDataFim(today);
        }

        Stage updated = stageRepository.save(stage);
        return stageMapper.toDTO(updated);
    }

    @Override
    public StageResponseDTO reopenStageById(Long id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etapa nao encontrada"));
        if (!Boolean.TRUE.equals(stage.getIsActive())) {
            throw new BusinessException("Nao e possivel reabrir uma etapa inativa");
        }

        stage.setDataConclusao(null);
        Stage updated = stageRepository.save(stage);
        return stageMapper.toDTO(updated);
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }

    private Long resolveGoalId(Stage stage, StageUpdateDTO dto) {
        if (dto.goalId() != null) {
            return dto.goalId();
        }
        if (stage.getGoal() == null || stage.getGoal().getId() == null) {
            throw new ResourceNotFoundException("Meta nao encontrada");
        }
        return stage.getGoal().getId();
    }

    private Goal findActiveGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta nao encontrada"));
        if (!Boolean.TRUE.equals(goal.getIsActive())) {
            throw new ResourceNotFoundException("Meta nao encontrada");
        }
        return goal;
    }

    private void normalizeFinancialFields(Stage stage, Goal goal, Long currentStageId) {
        if (!Boolean.TRUE.equals(stage.getHasFinancialValue())) {
            stage.setHasFinancialValue(false);
            stage.setFinancialAmount(null);
            return;
        }

        if (!Boolean.TRUE.equals(goal.getHasFinancialValue()) || goal.getFinancialAmount() == null) {
            throw new BusinessException("Defina primeiro o valor financeiro da meta antes de informar valor na etapa.");
        }

        BigDecimal amount = stage.getFinancialAmount();
        if (amount == null) {
            throw new BusinessException("Informe o valor financeiro da etapa.");
        }
        if (amount.signum() <= 0) {
            throw new BusinessException("O valor financeiro da etapa deve ser maior que zero.");
        }

        BigDecimal normalizedAmount = amount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal goalAmount = goal.getFinancialAmount().setScale(2, RoundingMode.HALF_UP);
        BigDecimal allocatedByOtherStages = stageRepository
                .sumActiveFinancialAmountByGoalIdExcludingStage(goal.getId(), currentStageId)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remainingAmount = goalAmount.subtract(allocatedByOtherStages);

        if (normalizedAmount.compareTo(remainingAmount) > 0) {
            BigDecimal suggestedAmount = remainingAmount.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
            if (suggestedAmount.signum() > 0) {
                throw new BusinessException(
                        "O valor da etapa nao pode superar o valor da meta. Valor restante para completar a meta: "
                                + formatCurrency(suggestedAmount) + "."
                );
            }
            throw new BusinessException("O valor da etapa nao pode superar o valor da meta. Esta meta ja esta totalmente preenchida.");
        }

        stage.setHasFinancialValue(true);
        stage.setFinancialAmount(normalizedAmount);
    }

    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(amount);
    }
}
