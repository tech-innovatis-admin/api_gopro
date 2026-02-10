package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ProjectMonthResponseDTO(
        int requestedMonth,
        int requestedYear,
        long totalContracts,
        BigDecimal totalValue,
        List<Integer> availableYears,
        List<ProjectMonthMetricDTO> months
) {
}
