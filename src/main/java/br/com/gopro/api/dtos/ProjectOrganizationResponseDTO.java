package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.StatusProjectOrganizationEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProjectOrganizationResponseDTO(
        Long id,
        Long project,
        Long organization,
        String contractNumber,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        StatusProjectOrganizationEnum statusProjectOrganization,
        BigDecimal totalValue,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {
}
