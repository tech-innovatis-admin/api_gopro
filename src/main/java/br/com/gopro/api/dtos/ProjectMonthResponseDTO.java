package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ProjectMonthResponseDTO(
        int requestedMonth,
        long totalContracts,
        BigDecimal totalValue,
        List<ProjectMonthMetricDTO> months
) {
}
