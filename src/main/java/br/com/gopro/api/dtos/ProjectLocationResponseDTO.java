package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ProjectLocationResponseDTO(
        String requestedLocation,
        long totalContracts,
        BigDecimal totalValue,
        List<ProjectLocationMetricDTO> locations
) {
}
