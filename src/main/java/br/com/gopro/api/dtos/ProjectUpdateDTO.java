package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectUpdateDTO(
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String name,
        @Size(max = 255, message = "Código deve ter no máximo 255 caracteres")
        String code,
        ProjectStatusEnum projectStatus,
        @Size(max = 255, message = "Área/segmento deve ter no máximo 255 caracteres")
        String areaSegmento,
        @Size(max = 1000, message = "Objeto deve ter no máximo 1000 caracteres")
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
                message = "Valor do projeto deve ter no máximo 13 digitos inteiros e 2 casas decimais"
        )
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate openingDate,
        LocalDate closingDate,
        @Size(max = 255, message = "Cidade deve ter no máximo 255 caracteres")
        String city,
        @Size(max = 255, message = "UF deve ter no máximo 255 caracteres")
        String state,
        @Size(max = 255, message = "Local de execução deve ter no máximo 255 caracteres")
        String executionLocation,
        Boolean executedByInnovatis,
        Long updatedBy
) {}
