package br.com.gopro.api.dtos;

import java.math.BigDecimal;

public record ProjectMonthMetricDTO(
        int month,
        String label,
        long contracts,
        BigDecimal totalValue
) {
}
