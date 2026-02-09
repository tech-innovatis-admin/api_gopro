package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectCategoryMetricDTO;
import br.com.gopro.api.dtos.ProjectLocationMetricDTO;
import br.com.gopro.api.dtos.ProjectLocationRequestDTO;
import br.com.gopro.api.dtos.ProjectLocationResponseDTO;
import br.com.gopro.api.dtos.ProjectMonthMetricDTO;
import br.com.gopro.api.dtos.ProjectMonthRequestDTO;
import br.com.gopro.api.dtos.ProjectMonthResponseDTO;
import br.com.gopro.api.dtos.ProjectPartnerMetricDTO;
import br.com.gopro.api.dtos.ProjectPartnerRequestDTO;
import br.com.gopro.api.dtos.ProjectPartnerResponseDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryRequestDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryResponseDTO;
import br.com.gopro.api.dtos.ProjectTypeDistributionRequestDTO;
import br.com.gopro.api.dtos.ProjectTypeDistributionResponseDTO;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.repository.ProjectRepository;
import br.com.gopro.api.utils.NormalizeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProjectAnalyticsServiceImpl implements ProjectAnalyticsService {

    private static final Locale PT_BR = new Locale("pt", "BR");

    private final ProjectRepository projectRepository;
    private final PartnerRepository partnerRepository;

    @Override
    public ProjectStatusCategoryResponseDTO getStatusCategoryAnalytics(ProjectStatusCategoryRequestDTO request) {
        if (request == null || request.getProjectStatus() == null) {
            throw new BusinessException("Status do projeto e obrigatorio");
        }

        List<ProjectCategoryMetricDTO> categories = buildCategoryMetrics(
                projectRepository.aggregateTypeDistribution(request.getProjectStatus())
        );

        return new ProjectStatusCategoryResponseDTO(
                request.getProjectStatus(),
                sumContracts(categories),
                sumValues(categories),
                categories
        );
    }

    @Override
    public ProjectTypeDistributionResponseDTO getTypeDistributionAnalytics(ProjectTypeDistributionRequestDTO request) {
        if (request == null || request.getProjectType() == null) {
            throw new BusinessException("Tipo do projeto e obrigatorio");
        }

        List<ProjectCategoryMetricDTO> categories = buildCategoryMetrics(
                projectRepository.aggregateTypeDistribution(null)
        );

        Map<ProjectTypeEnum, ProjectCategoryMetricDTO> byType = categories.stream()
                .collect(Collectors.toMap(ProjectCategoryMetricDTO::type, Function.identity()));

        ProjectCategoryMetricDTO requestedCategory = byType.getOrDefault(
                request.getProjectType(),
                new ProjectCategoryMetricDTO(request.getProjectType(), 0L, BigDecimal.ZERO, BigDecimal.ZERO)
        );

        return new ProjectTypeDistributionResponseDTO(
                request.getProjectType(),
                sumContracts(categories),
                sumValues(categories),
                requestedCategory.percentageOfTotal(),
                categories
        );
    }

    @Override
    public ProjectMonthResponseDTO getMonthAnalytics(ProjectMonthRequestDTO request) {
        if (request == null || request.getMonth() == null) {
            throw new BusinessException("Mes e obrigatorio");
        }

        int month = request.getMonth();
        if (month < 1 || month > 12) {
            throw new BusinessException("Mes deve estar entre 1 e 12");
        }

        Map<Integer, ProjectRepository.ProjectMonthSummaryProjection> byMonth =
                projectRepository.aggregateByMonth()
                        .stream()
                        .collect(Collectors.toMap(ProjectRepository.ProjectMonthSummaryProjection::getMonth, Function.identity()));

        List<ProjectMonthMetricDTO> months = IntStream.rangeClosed(1, 12)
                .mapToObj(currentMonth -> {
                    ProjectRepository.ProjectMonthSummaryProjection projection = byMonth.get(currentMonth);
                    return new ProjectMonthMetricDTO(
                            currentMonth,
                            Month.of(currentMonth).getDisplayName(TextStyle.SHORT, PT_BR),
                            safeContracts(projection == null ? null : projection.getContracts()),
                            safeValue(projection == null ? null : projection.getTotalValue())
                    );
                })
                .toList();

        ProjectMonthMetricDTO requestedMonth = months.stream()
                .filter(metric -> metric.month() == month)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Mes invalido"));

        return new ProjectMonthResponseDTO(
                month,
                requestedMonth.contracts(),
                requestedMonth.totalValue(),
                months
        );
    }

    @Override
    public ProjectLocationResponseDTO getLocationAnalytics(ProjectLocationRequestDTO request) {
        if (request == null) {
            throw new BusinessException("Localidade e obrigatoria");
        }

        String requestedLocation = NormalizeUtils.normalizeOrNull(request.getLocation());
        if (requestedLocation == null) {
            throw new BusinessException("Localidade e obrigatoria");
        }

        String locationSearchTerm = requestedLocation.toLowerCase(PT_BR);

        List<ProjectLocationMetricDTO> locations = projectRepository.aggregateByLocation(locationSearchTerm)
                .stream()
                .map(projection -> new ProjectLocationMetricDTO(
                        resolveLocation(projection.getCity(), projection.getState(), projection.getExecutionLocation()),
                        NormalizeUtils.normalizeOrNull(projection.getCity()),
                        NormalizeUtils.normalizeOrNull(projection.getState()),
                        safeContracts(projection.getContracts()),
                        safeValue(projection.getTotalValue())
                ))
                .sorted(Comparator
                        .comparing(ProjectLocationMetricDTO::contracts, Comparator.reverseOrder())
                        .thenComparing(ProjectLocationMetricDTO::totalValue, Comparator.reverseOrder())
                        .thenComparing(ProjectLocationMetricDTO::location, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return new ProjectLocationResponseDTO(
                requestedLocation,
                locations.stream().mapToLong(ProjectLocationMetricDTO::contracts).sum(),
                locations.stream().map(ProjectLocationMetricDTO::totalValue).reduce(BigDecimal.ZERO, BigDecimal::add),
                locations
        );
    }

    @Override
    public ProjectPartnerResponseDTO getPartnerAnalytics(ProjectPartnerRequestDTO request) {
        if (request == null || request.getPartnerId() == null || request.getPartnerId() <= 0) {
            throw new BusinessException("ID do parceiro deve ser maior que zero");
        }

        Long partnerId = request.getPartnerId();
        Partner partner = partnerRepository.findById(partnerId)
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Parceiro nao encontrado"));

        ProjectRepository.ProjectTotalsSummaryProjection totalsProjection =
                projectRepository.aggregateTotalsByPartner(partnerId);

        List<ProjectPartnerMetricDTO> partners = projectRepository.aggregateByPrimaryPartner()
                .stream()
                .map(projection -> new ProjectPartnerMetricDTO(
                        projection.getPartnerId(),
                        NormalizeUtils.normalizeOrNull(projection.getPartnerName()) == null
                                ? "Nao informado"
                                : projection.getPartnerName().trim(),
                        safeContracts(projection.getContracts()),
                        safeValue(projection.getTotalValue())
                ))
                .sorted(Comparator
                        .comparing(ProjectPartnerMetricDTO::contracts, Comparator.reverseOrder())
                        .thenComparing(ProjectPartnerMetricDTO::totalValue, Comparator.reverseOrder())
                        .thenComparing(ProjectPartnerMetricDTO::partnerName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return new ProjectPartnerResponseDTO(
                partnerId,
                Objects.requireNonNullElse(NormalizeUtils.normalizeOrNull(partner.getName()), "Nao informado"),
                safeContracts(totalsProjection == null ? null : totalsProjection.getContracts()),
                safeValue(totalsProjection == null ? null : totalsProjection.getTotalValue()),
                partners
        );
    }

    private List<ProjectCategoryMetricDTO> buildCategoryMetrics(
            List<ProjectRepository.ProjectTypeSummaryProjection> rows
    ) {
        Map<ProjectTypeEnum, ProjectRepository.ProjectTypeSummaryProjection> byType = rows.stream()
                .filter(row -> row.getProjectType() != null)
                .collect(Collectors.toMap(ProjectRepository.ProjectTypeSummaryProjection::getProjectType, Function.identity()));

        BigDecimal total = rows.stream()
                .filter(row -> row.getProjectType() != null)
                .map(ProjectRepository.ProjectTypeSummaryProjection::getTotalValue)
                .map(this::safeValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Arrays.stream(ProjectTypeEnum.values())
                .map(type -> {
                    ProjectRepository.ProjectTypeSummaryProjection row = byType.get(type);
                    BigDecimal totalValue = safeValue(row == null ? null : row.getTotalValue());
                    long contracts = safeContracts(row == null ? null : row.getContracts());
                    return new ProjectCategoryMetricDTO(
                            type,
                            contracts,
                            totalValue,
                            percentage(totalValue, total)
                    );
                })
                .toList();
    }

    private long sumContracts(List<ProjectCategoryMetricDTO> categories) {
        return categories.stream().mapToLong(ProjectCategoryMetricDTO::contracts).sum();
    }

    private BigDecimal sumValues(List<ProjectCategoryMetricDTO> categories) {
        return categories.stream()
                .map(ProjectCategoryMetricDTO::totalValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private long safeContracts(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal safeValue(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private String resolveLocation(String city, String state, String executionLocation) {
        String normalizedCity = NormalizeUtils.normalizeOrNull(city);
        String normalizedState = NormalizeUtils.normalizeOrNull(state);
        String normalizedExecutionLocation = NormalizeUtils.normalizeOrNull(executionLocation);

        if (normalizedCity != null && normalizedState != null) {
            return normalizedCity + " - " + normalizedState;
        }
        if (normalizedCity != null) {
            return normalizedCity;
        }
        if (normalizedState != null) {
            return normalizedState;
        }
        if (normalizedExecutionLocation != null) {
            return normalizedExecutionLocation;
        }
        return "Nao informado";
    }
}
