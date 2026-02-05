package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectResponseDTO(
        Long id,
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
        BigDecimal contractValue,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate openingDate,
        LocalDate closingDate,
        String city,
        String state,
        String executionLocation,
        BigDecimal totalReceived,
        BigDecimal totalExpenses,
        BigDecimal saldo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
