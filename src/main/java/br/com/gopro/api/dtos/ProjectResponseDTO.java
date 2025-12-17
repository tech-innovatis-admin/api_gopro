package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusProjectsEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectResponseDTO(
        Long id,
        String name,
        String code,
        StatusProjectsEnum statusProjects,
        String areaSegmento,
        Long orgaoFinanciador,
        Long executionOrg,
        String cordinator,
        String scope,
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate openingDate,
        String executionLocation,
        Long createdBy,
        Long updatedBy
) {
}
