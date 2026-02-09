package br.com.gopro.api.dtos;

import java.math.BigDecimal;

public record ProjectLocationMetricDTO(
        String location,
        String city,
        String state,
        long contracts,
        BigDecimal totalValue
) {
}
