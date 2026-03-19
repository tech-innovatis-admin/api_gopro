package br.com.gopro.api.service;

import br.com.gopro.api.dtos.StageRequestDTO;
import br.com.gopro.api.dtos.StageResponseDTO;
import br.com.gopro.api.dtos.StageUpdateDTO;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.mapper.StageMapper;
import br.com.gopro.api.model.Goal;
import br.com.gopro.api.model.Stage;
import br.com.gopro.api.repository.GoalRepository;
import br.com.gopro.api.repository.StageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageServiceImplTest {

    @Mock
    private StageRepository stageRepository;

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private StageMapper stageMapper;

    @InjectMocks
    private StageServiceImpl service;

    @Test
    void createStage_shouldPersistFinancialAmountWithinGoalLimit() {
        StageRequestDTO dto = new StageRequestDTO(
                11L,
                1,
                "Etapa 1",
                "Planejamento inicial",
                null,
                null,
                null,
                true,
                new BigDecimal("300.00"),
                7L
        );

        Stage mappedStage = new Stage();
        mappedStage.setNumero(1);
        mappedStage.setTitulo("Etapa 1");
        mappedStage.setDescricao("Planejamento inicial");
        mappedStage.setHasFinancialValue(true);
        mappedStage.setFinancialAmount(new BigDecimal("300.00"));

        Goal goal = goal(11L, true, "1000.00");

        when(stageMapper.toEntity(dto)).thenReturn(mappedStage);
        when(goalRepository.findById(11L)).thenReturn(Optional.of(goal));
        when(stageRepository.sumActiveFinancialAmountByGoalIdExcludingStage(11L, null))
                .thenReturn(new BigDecimal("450.00"));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> {
            Stage saved = invocation.getArgument(0);
            saved.setId(91L);
            return saved;
        });
        when(stageMapper.toDTO(any(Stage.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        StageResponseDTO result = service.createStage(dto);

        assertThat(mappedStage.getGoal()).isSameAs(goal);
        assertThat(mappedStage.getIsActive()).isTrue();
        assertThat(mappedStage.getHasFinancialValue()).isTrue();
        assertThat(mappedStage.getFinancialAmount()).isEqualByComparingTo("300.00");
        assertThat(result.goalId()).isEqualTo(11L);
        assertThat(result.hasFinancialValue()).isTrue();
        assertThat(result.financialAmount()).isEqualByComparingTo("300.00");
    }

    @Test
    void createStage_shouldRejectFinancialAmountThatExceedsGoalRemainder() {
        StageRequestDTO dto = new StageRequestDTO(
                11L,
                2,
                "Etapa 2",
                null,
                null,
                null,
                null,
                true,
                new BigDecimal("300.00"),
                7L
        );

        Stage mappedStage = new Stage();
        mappedStage.setNumero(2);
        mappedStage.setTitulo("Etapa 2");
        mappedStage.setHasFinancialValue(true);
        mappedStage.setFinancialAmount(new BigDecimal("300.00"));

        Goal goal = goal(11L, true, "1000.00");

        when(stageMapper.toEntity(dto)).thenReturn(mappedStage);
        when(goalRepository.findById(11L)).thenReturn(Optional.of(goal));
        when(stageRepository.sumActiveFinancialAmountByGoalIdExcludingStage(11L, null))
                .thenReturn(new BigDecimal("850.00"));

        assertThatThrownBy(() -> service.createStage(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("O valor da etapa nao pode superar o valor da meta.")
                .hasMessageContaining("150,00");
    }

    @Test
    void updateStage_shouldClearFinancialFieldsWhenFinancialLinkIsRemoved() {
        Goal goal = goal(11L, true, "1000.00");

        Stage existingStage = new Stage();
        existingStage.setId(45L);
        existingStage.setGoal(goal);
        existingStage.setNumero(3);
        existingStage.setTitulo("Etapa 3");
        existingStage.setIsActive(true);
        existingStage.setHasFinancialValue(true);
        existingStage.setFinancialAmount(new BigDecimal("400.00"));

        StageUpdateDTO dto = new StageUpdateDTO(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                8L
        );

        when(stageRepository.findById(45L)).thenReturn(Optional.of(existingStage));
        when(goalRepository.findById(11L)).thenReturn(Optional.of(goal));
        doAnswer(invocation -> {
            StageUpdateDTO update = invocation.getArgument(0);
            Stage stage = invocation.getArgument(1);
            if (update.hasFinancialValue() != null) {
                stage.setHasFinancialValue(update.hasFinancialValue());
            }
            if (update.financialAmount() != null) {
                stage.setFinancialAmount(update.financialAmount());
            }
            if (update.updatedBy() != null) {
                stage.setUpdatedBy(update.updatedBy());
            }
            return null;
        }).when(stageMapper).updateEntityFromDTO(any(StageUpdateDTO.class), any(Stage.class));
        when(stageRepository.save(any(Stage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(stageMapper.toDTO(any(Stage.class))).thenAnswer(invocation -> toDto(invocation.getArgument(0)));

        StageResponseDTO result = service.updateStageById(45L, dto);

        assertThat(existingStage.getHasFinancialValue()).isFalse();
        assertThat(existingStage.getFinancialAmount()).isNull();
        assertThat(result.hasFinancialValue()).isFalse();
        assertThat(result.financialAmount()).isNull();
    }

    private Goal goal(Long id, boolean hasFinancialValue, String financialAmount) {
        Goal goal = new Goal();
        goal.setId(id);
        goal.setIsActive(true);
        goal.setHasFinancialValue(hasFinancialValue);
        goal.setFinancialAmount(financialAmount == null ? null : new BigDecimal(financialAmount));
        return goal;
    }

    private StageResponseDTO toDto(Stage stage) {
        return new StageResponseDTO(
                stage.getId(),
                stage.getGoal() == null ? null : stage.getGoal().getId(),
                stage.getNumero(),
                stage.getTitulo(),
                stage.getDescricao(),
                stage.getDataInicio(),
                stage.getDataFim(),
                stage.getDataConclusao(),
                stage.getHasFinancialValue(),
                stage.getFinancialAmount(),
                stage.getIsActive(),
                stage.getCreatedAt(),
                stage.getUpdatedAt(),
                stage.getCreatedBy(),
                stage.getUpdatedBy()
        );
    }
}
