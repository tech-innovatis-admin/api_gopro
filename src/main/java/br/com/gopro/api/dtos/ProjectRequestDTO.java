package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectRequestDTO(
        @NotBlank(message = "Nome e obrigatorio") String name,
        String code,
        @NotNull(message = "Status e obrigatorio") ProjectStatusEnum projectStatus,
        String areaSegmento,
        @NotBlank(message = "Objeto e obrigatorio") String object,
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
        String city,
        @NotBlank(message = "UF e obrigatoria") String state,
        String executionLocation,
        Long createdBy
) {}
