package br.com.gopro.api.service;

import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectDashboardResponseDTO;
import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.mapper.ProjectMapper;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.PublicAgency;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import br.com.gopro.api.repository.PublicAgencyRepository;
import br.com.gopro.api.repository.SecretaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
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

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PublicAgencyRepository publicAgencyRepository;

    @Mock
    private SecretaryRepository secretaryRepository;

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private ProjectPeopleRepository projectPeopleRepository;

    @InjectMocks
    private ProjectServiceImpl service;

    @Test
    void createProject_shouldPersistExecutedByInnovatisFlag() {
        ProjectRequestDTO dto = new ProjectRequestDTO(
                "Contrato teste",
                null,
                ProjectStatusEnum.PRE_PROJETO,
                null,
                "Objeto teste",
                1L,
                null,
                2L,
                null,
                null,
                ProjectGovIfEnum.IF,
                ProjectTypeEnum.PROJETO,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null,
                "Teresina",
                "PI",
                "Teresina - PI",
                true,
                99L
        );

        Project mappedProject = new Project();
        mappedProject.setProjectStatus(ProjectStatusEnum.PRE_PROJETO);
        mappedProject.setProjectGovIf(ProjectGovIfEnum.IF);
        mappedProject.setProjectType(ProjectTypeEnum.PROJETO);
        mappedProject.setPrimaryPartner(new Partner());
        mappedProject.getPrimaryPartner().setId(1L);
        mappedProject.setPrimaryClient(new PublicAgency());
        mappedProject.getPrimaryClient().setId(2L);
        mappedProject.setExecutedByInnovatis(false);

        Partner activePartner = new Partner();
        activePartner.setId(1L);
        activePartner.setIsActive(true);

        PublicAgency activeAgency = new PublicAgency();
        activeAgency.setId(2L);
        activeAgency.setIsActive(true);

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(activePartner));
        when(publicAgencyRepository.findById(2L)).thenReturn(Optional.of(activeAgency));
        when(projectRepository.findRecentCreatedProjects(any(), any())).thenReturn(List.of());
        when(projectMapper.toEntity(dto)).thenReturn(mappedProject);
        when(projectRepository.findCodesByPrefix("PROJIF/PI-2026")).thenReturn(List.of());
        when(projectRepository.saveAndFlush(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(15L);
            return project;
        });
        when(projectMapper.toDTO(any(Project.class))).thenReturn(new ProjectResponseDTO(
                15L,
                "Contrato teste",
                "PROJIF/PI-20260001",
                ProjectStatusEnum.PRE_PROJETO,
                null,
                "Objeto teste",
                1L,
                null,
                2L,
                null,
                null,
                ProjectGovIfEnum.IF,
                ProjectTypeEnum.PROJETO,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null,
                "Teresina",
                "PI",
                "Teresina - PI",
                true,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                null,
                null,
                99L,
                null
        ));

        ProjectResponseDTO result = service.createProject(dto);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).saveAndFlush(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getExecutedByInnovatis()).isTrue();
        assertThat(projectCaptor.getValue().getCreatedBy()).isEqualTo(99L);
        assertThat(result.executedByInnovatis()).isTrue();
    }

    @Test
    void createProject_shouldReturnRecentEquivalentProject_withoutCreatingDuplicate() {
        ProjectRequestDTO dto = new ProjectRequestDTO(
                "Contrato teste",
                null,
                ProjectStatusEnum.PRE_PROJETO,
                "Tecnologia",
                "Objeto teste",
                1L,
                null,
                2L,
                null,
                null,
                ProjectGovIfEnum.IF,
                ProjectTypeEnum.PROJETO,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null,
                "Teresina",
                "PI",
                "Teresina - PI",
                true,
                99L
        );

        Partner activePartner = new Partner();
        activePartner.setId(1L);
        activePartner.setIsActive(true);

        PublicAgency activeAgency = new PublicAgency();
        activeAgency.setId(2L);
        activeAgency.setIsActive(true);

        Project existingProject = new Project();
        existingProject.setId(22L);
        existingProject.setName("Contrato teste");
        existingProject.setProjectStatus(ProjectStatusEnum.PRE_PROJETO);
        existingProject.setAreaSegmento("Tecnologia");
        existingProject.setObject("Objeto teste");
        existingProject.setProjectGovIf(ProjectGovIfEnum.IF);
        existingProject.setProjectType(ProjectTypeEnum.PROJETO);
        existingProject.setContractValue(new BigDecimal("1000.00"));
        existingProject.setStartDate(LocalDate.of(2026, 3, 1));
        existingProject.setEndDate(LocalDate.of(2026, 12, 31));
        existingProject.setCity("Teresina");
        existingProject.setState("PI");
        existingProject.setExecutionLocation("Teresina - PI");
        existingProject.setExecutedByInnovatis(true);
        existingProject.setCreatedBy(99L);
        existingProject.setCreatedAt(LocalDateTime.now());
        existingProject.setIsActive(true);

        Partner primaryPartner = new Partner();
        primaryPartner.setId(1L);
        existingProject.setPrimaryPartner(primaryPartner);

        PublicAgency primaryClient = new PublicAgency();
        primaryClient.setId(2L);
        existingProject.setPrimaryClient(primaryClient);

        ProjectResponseDTO existingResponse = new ProjectResponseDTO(
                22L,
                "Contrato teste",
                "PROJIF/PI-20260001",
                ProjectStatusEnum.PRE_PROJETO,
                "Tecnologia",
                "Objeto teste",
                1L,
                null,
                2L,
                null,
                null,
                ProjectGovIfEnum.IF,
                ProjectTypeEnum.PROJETO,
                new BigDecimal("1000.00"),
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 12, 31),
                null,
                null,
                "Teresina",
                "PI",
                "Teresina - PI",
                true,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                LocalDateTime.now(),
                99L,
                null
        );

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(activePartner));
        when(publicAgencyRepository.findById(2L)).thenReturn(Optional.of(activeAgency));
        when(projectRepository.findRecentCreatedProjects(any(), any())).thenReturn(List.of(existingProject));
        when(projectMapper.toDTO(existingProject)).thenReturn(existingResponse);

        ProjectResponseDTO result = service.createProject(dto);

        assertThat(result.id()).isEqualTo(22L);
        verify(projectRepository, never()).saveAndFlush(any(Project.class));
        verify(projectMapper, never()).toEntity(dto);
    }

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
    void getDashboard_shouldFilterByExecutedByInnovatis() {
        Project projectA = project(
                1L,
                ProjectStatusEnum.EXECUCAO,
                ProjectTypeEnum.PROJETO,
                "100.00",
                LocalDate.of(2026, 1, 10),
                "Teresina",
                "PI",
                1L,
                "FADEX",
                null
        );
        projectA.setExecutedByInnovatis(true);

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
        projectB.setExecutedByInnovatis(false);

        when(projectRepository.findByIsActiveTrue()).thenReturn(List.of(projectA, projectB));

        ProjectDashboardResponseDTO result = service.getDashboard(
                null,
                null,
                null,
                true,
                null,
                null,
                null,
                null
        );

        assertThat(result.summary().totalContracts()).isEqualTo(1);
        assertThat(result.summary().totalValue()).isEqualByComparingTo("100.00");
        assertThat(result.filters().executedByInnovatis()).isTrue();
    }

    @Test
    void getDashboard_shouldThrowBusinessException_whenMonthOutOfRange() {
        assertThatThrownBy(() -> service.getDashboard(null, null, null, null, 13, null, null, null))
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

        ProjectDashboardResponseDTO result = service.getDashboard(null, null, null, null, null, null, null, null);

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
        project.setExecutedByInnovatis(false);

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
