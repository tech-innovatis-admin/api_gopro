package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectMonthRequestDTO;
import br.com.gopro.api.dtos.ProjectMonthResponseDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryRequestDTO;
import br.com.gopro.api.dtos.ProjectStatusCategoryResponseDTO;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectAnalyticsServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private ProjectAnalyticsServiceImpl service;

    @Test
    void getStatusCategoryAnalytics_shouldReturnTotalsForRequestedStatus() {
        ProjectStatusCategoryRequestDTO request = new ProjectStatusCategoryRequestDTO();
        request.setProjectStatus(ProjectStatusEnum.EXECUCAO);

        when(projectRepository.aggregateTypeDistribution(ProjectStatusEnum.EXECUCAO))
                .thenReturn(List.of(
                        typeSummary(ProjectTypeEnum.PROJETO, 2L, "300.00"),
                        typeSummary(ProjectTypeEnum.PRODUTO, 1L, "100.00")
                ));

        ProjectStatusCategoryResponseDTO result = service.getStatusCategoryAnalytics(request);

        assertThat(result.totalContracts()).isEqualTo(3L);
        assertThat(result.totalValue()).isEqualByComparingTo("400.00");
        assertThat(result.categories()).hasSize(2);
        assertThat(result.categories().get(0).percentageOfTotal()
                .add(result.categories().get(1).percentageOfTotal()))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void getMonthAnalytics_shouldReturnRequestedMonthAndCompleteSeries() {
        ProjectMonthRequestDTO request = new ProjectMonthRequestDTO();
        request.setMonth(1);
        request.setYear(2026);

        when(projectRepository.findAvailableYearsForMonthAnalytics())
                .thenReturn(List.of(2026, 2025));
        when(projectRepository.aggregateByMonth(2026))
                .thenReturn(List.of(monthSummary(1, 4L, "850.00")));

        ProjectMonthResponseDTO result = service.getMonthAnalytics(request);

        assertThat(result.requestedMonth()).isEqualTo(1);
        assertThat(result.requestedYear()).isEqualTo(2026);
        assertThat(result.availableYears()).containsExactly(2026, 2025);
        assertThat(result.totalContracts()).isEqualTo(4L);
        assertThat(result.totalValue()).isEqualByComparingTo("850.00");
        assertThat(result.months()).hasSize(12);
    }

    @Test
    void getPartnerAnalytics_shouldThrowNotFoundWhenPartnerDoesNotExist() {
        when(partnerRepository.findById(999L)).thenReturn(Optional.empty());

        var request = new br.com.gopro.api.dtos.ProjectPartnerRequestDTO();
        request.setPartnerId(999L);

        assertThatThrownBy(() -> service.getPartnerAnalytics(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Parceiro nao encontrado");
    }

    private ProjectRepository.ProjectTypeSummaryProjection typeSummary(
            ProjectTypeEnum type,
            Long contracts,
            String totalValue
    ) {
        BigDecimal parsedTotalValue = new BigDecimal(totalValue);

        return new ProjectRepository.ProjectTypeSummaryProjection() {
            @Override
            public ProjectTypeEnum getProjectType() {
                return type;
            }

            @Override
            public Long getContracts() {
                return contracts;
            }

            @Override
            public BigDecimal getTotalValue() {
                return parsedTotalValue;
            }
        };
    }

    private ProjectRepository.ProjectMonthSummaryProjection monthSummary(
            Integer month,
            Long contracts,
            String totalValue
    ) {
        BigDecimal parsedTotalValue = new BigDecimal(totalValue);

        return new ProjectRepository.ProjectMonthSummaryProjection() {
            @Override
            public Integer getMonth() {
                return month;
            }

            @Override
            public Long getContracts() {
                return contracts;
            }

            @Override
            public BigDecimal getTotalValue() {
                return parsedTotalValue;
            }
        };
    }
}
