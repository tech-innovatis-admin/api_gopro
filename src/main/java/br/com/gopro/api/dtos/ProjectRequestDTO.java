package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectRequestDTO(
        @NotBlank(message = "Nome e obrigatorio") String name,
        @NotBlank(message = "Codigo e obrigatorio") String code,
        @NotNull(message = "Status e obrigatorio") ProjectStatusEnum projectStatus,
        String areaSegmento,
        @NotBlank(message = "Objeto e obrigatorio") String object,
        @NotNull(message = "Parceiro primario e obrigatorio") Long primaryPartnerId,
        Long secundaryPartnerId,
        @NotNull(message = "Cliente primario e obrigatorio") Long primaryClientId,
        Long secundaryClientId,
        Long cordinatorId,
        ProjectGovIfEnum projectGovIf,
        ProjectTypeEnum projectType,
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate openingDate,
        LocalDate closingDate,
        String city,
        String state,
        String executionLocation,
        Long createdBy
) {}
