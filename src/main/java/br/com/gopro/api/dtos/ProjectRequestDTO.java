package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusProjectsEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectRequestDTO(
        @NotBlank(message = "Nome é obrigatório!")
        String name,

        String code,

        @NotBlank(message = "Status do Projeto é obrigatório")
        StatusProjectsEnum statusProjects,

        String areaSegmento,

        @NotNull(message = "Orgão financiador é obrigatório")
        Long orgaoFinanciador,

        @NotNull(message = "Orgão executor é obrigatório")
        Long executionOrg,

        String cordinator,

        String scope,

        BigDecimal contractValue,

        LocalDate startDate,

        LocalDate endDate,

        LocalDate openingDate,

        String executionLocation,

        @NotNull(message = "Usuário que criou o projeto é obrigatório")
        Long createdBy,

        Long updatedBy
) {
}
