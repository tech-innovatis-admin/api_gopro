package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectStatusEnum;

import java.math.BigDecimal;
import java.util.List;

public record ProjectStatusCategoryResponseDTO(
        ProjectStatusEnum projectStatus,
        long totalContracts,
        BigDecimal totalValue,
        List<ProjectCategoryMetricDTO> categories
) {
}
