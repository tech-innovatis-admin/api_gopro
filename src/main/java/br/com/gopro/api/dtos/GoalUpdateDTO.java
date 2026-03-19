package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GoalUpdateDTO(
        Long projectId,
        Integer numero,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate dataConclusao,
        Boolean hasFinancialValue,
        BigDecimal financialAmount,
        Long updatedBy
) {}
