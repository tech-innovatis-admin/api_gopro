package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;

import java.math.BigDecimal;
import java.util.List;

public record ProjectDashboardResponseDTO(
        FilterDTO filters,
        SummaryDTO summary,
        List<StatusMetricDTO> byStatus,
        List<TypeMetricDTO> byType,
        List<MonthMetricDTO> byMonth,
        List<LocationMetricDTO> byLocation,
        List<PartnerMetricDTO> byPartner
) {
    public record FilterDTO(
            ProjectStatusEnum projectStatus,
            ProjectTypeEnum projectType,
            Integer month,
            String location,
            Long partnerId
    ) {
    }

    public record SummaryDTO(
            long totalContracts,
            BigDecimal totalValue
    ) {
    }

    public record StatusMetricDTO(
            ProjectStatusEnum status,
            long contracts,
            BigDecimal totalValue
    ) {
    }

    public record TypeMetricDTO(
            ProjectTypeEnum type,
            long contracts,
            BigDecimal totalValue,
            BigDecimal percentageOfTypeTotal
    ) {
    }

    public record MonthMetricDTO(
            int month,
            String label,
            long contracts,
            BigDecimal totalValue
    ) {
    }

    public record LocationMetricDTO(
            String location,
            String city,
            String state,
            long contracts,
            BigDecimal totalValue
    ) {
    }

    public record PartnerMetricDTO(
            Long partnerId,
            String partnerName,
            long contracts,
            BigDecimal totalValue
    ) {
    }
}
