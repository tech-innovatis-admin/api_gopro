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
        @NotBlank(message = "Titulo e obrigatorio")
        @Size(max = 255, message = "Titulo deve ter no maximo 255 caracteres")
        String name,
        @Size(max = 255, message = "Codigo deve ter no maximo 255 caracteres")
        String code,
        @NotNull(message = "Status e obrigatorio") ProjectStatusEnum projectStatus,
        @Size(max = 255, message = "Area/segmento deve ter no maximo 255 caracteres")
        String areaSegmento,
        @NotBlank(message = "Objeto e obrigatorio")
        @Size(max = 1000, message = "Objeto deve ter no maximo 1000 caracteres")
        String object,
        @NotNull(message = "Parceiro primario e obrigatorio") Long primaryPartnerId,
        Long secundaryPartnerId,
        @NotNull(message = "Cliente primario e obrigatorio") Long primaryClientId,
        Long secundaryClientId,
        Long cordinatorId,
        @NotNull(message = "Unidade GOV/IF e obrigatoria") ProjectGovIfEnum projectGovIf,
        @NotNull(message = "Tipo do contrato e obrigatorio") ProjectTypeEnum projectType,
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
        @Size(max = 255, message = "Cidade deve ter no maximo 255 caracteres")
        String city,
        @NotBlank(message = "UF e obrigatoria")
        @Size(max = 255, message = "UF deve ter no maximo 255 caracteres")
        String state,
        @Size(max = 255, message = "Local de execucao deve ter no maximo 255 caracteres")
        String executionLocation,
        @NotNull(message = "Informe se o projeto e executado pela Innovatis") Boolean executedByInnovatis,
        Long createdBy
) {}
