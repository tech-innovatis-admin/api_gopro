package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ContractingStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectCompanyDetailedResponseDTO(
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
        Long updatedBy,
        String companyName,
        String companyTradeName,
        String companyCnpj,
        String companyEmail,
        String companyPhone,
        String companyAddress,
        String companyCity,
        String companyState,
        Long companyResponsiblePersonId,
        String companyResponsiblePersonFullName,
        String companyResponsiblePersonCpf,
        String companyResponsiblePersonEmail,
        BigDecimal availableBalance,
        BigDecimal executionPercentage
) {
}
