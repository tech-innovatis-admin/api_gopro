package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String name,
        @Size(max = 255, message = "Código deve ter no máximo 255 caracteres")
        String code,
        @NotNull(message = "Status é obrigatório") ProjectStatusEnum projectStatus,
        @Size(max = 255, message = "Área/segmento deve ter no máximo 255 caracteres")
        String areaSegmento,
        @NotBlank(message = "Objeto é obrigatório")
        @Size(max = 1000, message = "Objeto deve ter no máximo 1000 caracteres")
        String object,
        @NotNull(message = "Parceiro primário é obrigatório") Long primaryPartnerId,
        Long secundaryPartnerId,
        @NotNull(message = "Cliente primário é obrigatório") Long primaryClientId,
        Long secundaryClientId,
        Long cordinatorId,
        @NotNull(message = "Unidade GOV/IF é obrigatória") ProjectGovIfEnum projectGovIf,
        @NotNull(message = "Tipo do contrato é obrigatório") ProjectTypeEnum projectType,
        @DecimalMin(value = "0.01", message = "Valor do projeto deve ser maior que zero")
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Valor do projeto deve ter no máximo 13 dígitos inteiros e 2 casas decimais"
        )
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate openingDate,
        LocalDate closingDate,
        @Size(max = 255, message = "Cidade deve ter no máximo 255 caracteres")
        String city,
        @NotBlank(message = "UF é obrigatória")
        @Size(max = 255, message = "UF deve ter no máximo 255 caracteres")
        String state,
        @Size(max = 255, message = "Local de execução deve ter no máximo 255 caracteres")
        String executionLocation,
        @NotNull(message = "Informe se o projeto é executado pela Innovatis") Boolean executedByInnovatis,
        Long createdBy
) {}
