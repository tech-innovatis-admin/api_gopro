package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectCompanyUpdateDTO(
        Long projectId,
        Long companyId,
        String contractNumber,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Short status,
        BigDecimal totalValue,
        String notes,
        Boolean isIncubated,
        String serviceType,
        Long updatedBy
) {}