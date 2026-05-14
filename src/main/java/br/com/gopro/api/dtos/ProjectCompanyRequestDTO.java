package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractingStatusEnum;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectCompanyRequestDTO(
        @NotNull(message = "Projeto e obrigatorio") Long projectId,
        @NotNull(message = "Empresa e obrigatoria") Long companyId,
        String contractNumber,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        ContractingStatusEnum status,
        BigDecimal totalValue,
        String notes,
        Boolean isIncubated,
        String serviceType,
        Long createdBy
) {}
