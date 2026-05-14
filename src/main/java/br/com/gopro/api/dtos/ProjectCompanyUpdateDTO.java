package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractingStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectCompanyUpdateDTO(
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
        Long updatedBy
) {}
