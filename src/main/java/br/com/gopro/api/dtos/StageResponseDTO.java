package br.com.gopro.api.dtos;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StageResponseDTO(
        Long id,
        Long goalId,
        Integer numero,
        String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate dataConclusao,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
