package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectUpdateDTO(
        String name,
        String code,
        ProjectStatusEnum projectStatus,
        String areaSegmento,
        String object,
        Long primaryPartnerId,
        Long secundaryPartnerId,
        Long primaryClientId,
        Long secundaryClientId,
        Long cordinatorId,
        ProjectGovIfEnum projectGovIf,
        ProjectTypeEnum projectType,
        @DecimalMin(value = "0.01", message = "Valor do projeto deve ser maior que zero")
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Valor do projeto deve ter no maximo 13 digitos inteiros e 2 casas decimais"
        )
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate openingDate,
        LocalDate closingDate,
        String city,
        String state,
        String executionLocation,
        Boolean executedByInnovatis,
        Long updatedBy
) {}
