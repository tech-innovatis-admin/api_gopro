package br.com.gopro.api.dtos;

import java.time.LocalDate;

public record PhaseUpdateDTO(
        Long stageId,
        Integer numero,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        Long updatedBy
) {}