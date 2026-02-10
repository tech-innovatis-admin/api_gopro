package br.com.gopro.api.dtos;

import java.time.LocalDate;

public record GoalUpdateDTO(
        Long projectId,
        Integer numero,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate dataConclusao,
        Long updatedBy
) {}
