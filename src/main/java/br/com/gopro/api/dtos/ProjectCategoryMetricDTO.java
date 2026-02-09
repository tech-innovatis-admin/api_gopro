package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectTypeEnum;

import java.math.BigDecimal;

public record ProjectCategoryMetricDTO(
        ProjectTypeEnum type,
        long contracts,
        BigDecimal totalValue,
        BigDecimal percentageOfTotal
) {
}
