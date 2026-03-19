package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalResponseDTO(
        Long id,
        Long projectId,
        Integer numero,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate dataConclusao,
        Boolean hasFinancialValue,
        BigDecimal financialAmount,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
