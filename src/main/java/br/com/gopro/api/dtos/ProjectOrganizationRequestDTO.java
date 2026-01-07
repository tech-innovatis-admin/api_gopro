package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusProjectOrganizationEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectOrganizationRequestDTO(
        @NotNull(message = "ID do projeto é obrigatório")
        Long project,

        @NotNull(message = "ID da organização é obrigatório")
        Long organization,

        String contractNumber,

        String description,

        LocalDate startDate,

        LocalDate endDate,

        @NotBlank(message = "Status do projeto é obrigatório")
        StatusProjectOrganizationEnum statusProjectOrganization,

        BigDecimal totalValue,

        String notes,

        Long createdBy,

        Long updatedBy
) {
}
