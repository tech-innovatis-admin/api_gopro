package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectDashboardResponseDTO;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.mapper.ProjectMapper;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private ProjectServiceImpl service;

    @Test
    void getDashboard_shouldFilterByStatusAndMonth_andCalculateTypePercentage() {
        Project projectA = project(
                1L,
                ProjectStatusEnum.PRE_PROJETO,
                ProjectTypeEnum.PROJETO,
                "100.00",
                LocalDate.of(2026, 1, 10),
                "Teresina",
                "PI",
                1L,
                "FADEX",
                2L
        );
        Project projectB = project(
                2L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PRODUTO,
                "300.00",
                LocalDate.of(2026, 1, 20),
                "Fortaleza",
                "CE",
                2L,
                "FAPTO",
                null
        );
        Project projectC = project(
                3L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PROJETO,
                "600.00",
                LocalDate.of(2026, 2, 5),
                "Teresina",
                "PI",
                1L,
                "FADEX",
                null
        );

        when(projectRepository.findByIsActiveTrue()).thenReturn(List.of(projectA, projectB, projectC));

        ProjectDashboardResponseDTO result = service.getDashboard(
                ProjectStatusEnum.EXECUCAO,
                null,
                null,
                1,
                null,
                null,
                null
        );

        assertThat(result.summary().totalContracts()).isEqualTo(1);
        assertThat(result.summary().totalValue()).isEqualByComparingTo("300.00");

        ProjectDashboardResponseDTO.TypeMetricDTO produtoMetric = result.byType().stream()
                .filter(metric -> metric.type() == ProjectTypeEnum.PRODUTO)
                .findFirst()
                .orElseThrow();
        ProjectDashboardResponseDTO.TypeMetricDTO projetoMetric = result.byType().stream()
                .filter(metric -> metric.type() == ProjectTypeEnum.PROJETO)
                .findFirst()
                .orElseThrow();

        assertThat(produtoMetric.contracts()).isEqualTo(1);
        assertThat(produtoMetric.totalValue()).isEqualByComparingTo("300.00");
        assertThat(produtoMetric.percentageOfTypeTotal()).isEqualByComparingTo("100.00");

        assertThat(projetoMetric.contracts()).isZero();
        assertThat(projetoMetric.totalValue()).isEqualByComparingTo("0.00");
        assertThat(projetoMetric.percentageOfTypeTotal()).isEqualByComparingTo("0.00");
        assertThat(result.expiringContracts().upToSixMonths()).isZero();
        assertThat(result.expiringContracts().upToOneYear()).isZero();
    }

    @Test
    void getDashboard_shouldFilterByLocationAndPartner() {
        Project projectA = project(
                1L,
                ProjectStatusEnum.PRE_PROJETO,
                ProjectTypeEnum.PROJETO,
                "100.00",
                LocalDate.of(2026, 1, 10),
                "Teresina",
                "PI",
                1L,
                "FADEX",
                2L
        );
        Project projectB = project(
                2L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PRODUTO,
                "300.00",
                LocalDate.of(2026, 1, 20),
                "Fortaleza",
                "CE",
                2L,
                "FAPTO",
                null
        );
        Project projectC = project(
                3L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PROJETO,
                "600.00",
                LocalDate.of(2026, 2, 5),
                "Teresina",
                "PI",
                1L,
                "FADEX",
                null
        );

        when(projectRepository.findByIsActiveTrue()).thenReturn(List.of(projectA, projectB, projectC));

        ProjectDashboardResponseDTO result = service.getDashboard(
                null,
                null,
                null,
                null,
                null,
                "teresina",
                1L
        );

        assertThat(result.summary().totalContracts()).isEqualTo(2);
        assertThat(result.summary().totalValue()).isEqualByComparingTo("700.00");
        assertThat(result.byLocation()).isNotEmpty();
        assertThat(result.byLocation().get(0).location()).containsIgnoringCase("Teresina");
    }

    @Test
    void getDashboard_shouldThrowBusinessException_whenMonthOutOfRange() {
        assertThatThrownBy(() -> service.getDashboard(null, null, null, 13, null, null, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Mes deve estar entre 1 e 12");
    }

    @Test
    void getDashboard_shouldBuildExpiringContractsWindows() {
        LocalDate today = LocalDate.now();

        Project expiringIn10Days = project(
                1L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PROJETO,
                "100.00",
                today.minusDays(5),
                "Teresina",
                "PI",
                1L,
                "FADEX",
                null
        );
        expiringIn10Days.setEndDate(today.plusDays(10));

        Project expiringIn50Days = project(
                2L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PRODUTO,
                "300.00",
                today.minusDays(20),
                "Fortaleza",
                "CE",
                2L,
                "FAPTO",
                null
        );
        expiringIn50Days.setEndDate(today.plusDays(50));

        Project expiringIn160Days = project(
                3L,
                ProjectStatusEnum.PRE_PROJETO,
                ProjectTypeEnum.PROJETO,
                "450.00",
                today.minusDays(30),
                "Recife",
                "PE",
                3L,
                "FUNDAJ",
                null
        );
        expiringIn160Days.setEndDate(today.plusDays(160));

        Project expiringIn220Days = project(
                4L,
                ProjectStatusEnum.PRE_PROJETO,
                ProjectTypeEnum.PROJETO,
                "700.00",
                today.minusDays(60),
                "Natal",
                "RN",
                4L,
                "UFRN",
                null
        );
        expiringIn220Days.setEndDate(today.plusDays(220));

        Project alreadyExpired = project(
                5L,
                ProjectStatusEnum.SUSPENSO,
                ProjectTypeEnum.PROJETO,
                "50.00",
                today.minusDays(120),
                "Maceio",
                "AL",
                5L,
                "UFAL",
                null
        );
        alreadyExpired.setEndDate(today.minusDays(3));

        when(projectRepository.findByIsActiveTrue()).thenReturn(
                List.of(expiringIn10Days, expiringIn50Days, expiringIn160Days, expiringIn220Days, alreadyExpired)
        );

        ProjectDashboardResponseDTO result = service.getDashboard(null, null, null, null, null, null, null);

        assertThat(result.expiringContracts().upToOneMonth()).isEqualTo(1);
        assertThat(result.expiringContracts().upToThreeMonths()).isEqualTo(1);
        assertThat(result.expiringContracts().upToSixMonths()).isEqualTo(1);
        assertThat(result.expiringContracts().upToOneYear()).isEqualTo(1);
        assertThat(result.expiringContracts().contracts())
                .extracting(ProjectDashboardResponseDTO.ExpiringContractDTO::projectId)
                .containsExactly(1L, 2L, 3L, 4L);
    }

    private Project project(
            Long id,
            ProjectStatusEnum status,
            ProjectTypeEnum type,
            String contractValue,
            LocalDate startDate,
            String city,
            String state,
            Long primaryPartnerId,
            String primaryPartnerName,
            Long secondaryPartnerId
    ) {
        Project project = new Project();
        project.setId(id);
        project.setProjectStatus(status);
        project.setProjectType(type);
        project.setContractValue(new BigDecimal(contractValue));
        project.setStartDate(startDate);
        project.setCity(city);
        project.setState(state);
        project.setIsActive(true);

        Partner primaryPartner = new Partner();
        primaryPartner.setId(primaryPartnerId);
        primaryPartner.setName(primaryPartnerName);
        project.setPrimaryPartner(primaryPartner);

        if (secondaryPartnerId != null) {
            Partner secondaryPartner = new Partner();
            secondaryPartner.setId(secondaryPartnerId);
            secondaryPartner.setName("Parceiro " + secondaryPartnerId);
            project.setSecundaryPartner(secondaryPartner);
        }

        return project;
    }
}
