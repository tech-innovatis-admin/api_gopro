package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.enums.ProjectGovIfEnum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProjectDashboardResponseDTO(
        FilterDTO filters,
        List<Integer> availableYears,
        SummaryDTO summary,
        List<StatusMetricDTO> byStatus,
        List<TypeMetricDTO> byType,
        List<MonthMetricDTO> byMonth,
        List<LocationMetricDTO> byLocation,
        List<PartnerMetricDTO> byPartner,
        ExpiringContractsDTO expiringContracts
) {
    public record FilterDTO(
            ProjectStatusEnum projectStatus,
            ProjectTypeEnum projectType,
            ProjectGovIfEnum projectGovIf,
            Boolean executedByInnovatis,
            Integer month,
            Integer year,
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
            String partnerAcronym,
            String partnerName,
            long contracts,
            BigDecimal totalValue
    ) {
    }

    public record ExpiringContractsDTO(
            LocalDate referenceDate,
            long upToOneMonth,
            long upToThreeMonths,
            long upToSixMonths,
            long upToOneYear,
            List<ExpiringContractDTO> contracts
    ) {
    }

    public record ExpiringContractDTO(
            Long projectId,
            String projectName,
            String projectCode,
            String primaryClientName,
            LocalDate endDate,
            long daysToExpiration,
            ProjectStatusEnum projectStatus,
            BigDecimal contractValue
    ) {
    }
}
