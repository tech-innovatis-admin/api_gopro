package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectTypeEnum;

import java.math.BigDecimal;
import java.util.List;

public record ProjectTypeDistributionResponseDTO(
        ProjectTypeEnum requestedType,
        long totalContracts,
        BigDecimal totalValue,
        BigDecimal requestedTypePercentage,
        List<ProjectCategoryMetricDTO> categories
) {
}
