package br.com.gopro.api.dtos;

import java.math.BigDecimal;

public record ProjectPartnerMetricDTO(
        Long partnerId,
        String partnerName,
        long contracts,
        BigDecimal totalValue
) {
}
