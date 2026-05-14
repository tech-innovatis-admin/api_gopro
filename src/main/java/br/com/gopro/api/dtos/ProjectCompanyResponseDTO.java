package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractingStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectCompanyResponseDTO(
        Long id,
        Long projectId,
        Long companyId,
        String contractNumber,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ContractingStatusEnum status,
        BigDecimal totalValue,
        String notes,
        Boolean isIncubated,
        String serviceType,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
