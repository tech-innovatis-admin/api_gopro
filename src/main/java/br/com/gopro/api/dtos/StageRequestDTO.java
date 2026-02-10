package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StageRequestDTO(
        @NotNull(message = "Meta e obrigatoria") Long goalId,
        @NotNull(message = "Numero e obrigatorio") Integer numero,
        @NotBlank(message = "Titulo e obrigatorio") String titulo,
        String descricao,
        LocalDate dataInicio,
        LocalDate dataFim,
        LocalDate dataConclusao,
        Long createdBy
) {}
