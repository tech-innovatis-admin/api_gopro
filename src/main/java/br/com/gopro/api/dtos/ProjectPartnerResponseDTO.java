package br.com.gopro.api.dtos;

import java.math.BigDecimal;
import java.util.List;

public record ProjectPartnerResponseDTO(
        Long requestedPartnerId,
        String requestedPartnerName,
        long totalContracts,
        BigDecimal totalValue,
        List<ProjectPartnerMetricDTO> partners
) {
}
