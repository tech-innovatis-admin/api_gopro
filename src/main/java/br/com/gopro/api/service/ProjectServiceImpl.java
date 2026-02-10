package br.com.gopro.api.service;

import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectDashboardResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectTotalsDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ProjectMapper;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import br.com.gopro.api.repository.PublicAgencyRepository;
import br.com.gopro.api.repository.SecretaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final PartnerRepository partnerRepository;
    private final PublicAgencyRepository publicAgencyRepository;
    private final SecretaryRepository secretaryRepository;
    private final PeopleRepository peopleRepository;
    private static final Locale PT_BR = new Locale("pt", "BR");
    @Value("${app.project.max-contract-value:9999999999999.99}")
    private BigDecimal maxContractValue;

    @Override
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        validateContractValue(dto.contractValue());
        validateReferencesForCreate(dto);

        Project project = projectMapper.toEntity(dto);
        project.setIsActive(true);
        if (project.getTotalReceived() == null) {
            project.setTotalReceived(BigDecimal.ZERO);
        }
        if (project.getTotalExpenses() == null) {
            project.setTotalExpenses(BigDecimal.ZERO);
        }
        if (project.getSaldo() == null) {
            project.setSaldo(BigDecimal.ZERO);
        }
        Project saved = projectRepository.save(project);
        return projectMapper.toDTO(saved);
    }

    @Override
    public PageResponseDTO<ProjectResponseDTO> listAllProjects(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> pageResult = projectRepository.findByIsActiveTrue(pageable);
        List<ProjectResponseDTO> content = pageResult.getContent().stream()
                .map(projectMapper::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public ProjectResponseDTO findProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new ResourceNotFoundException("Projeto nao encontrado");
        }
        return projectMapper.toDTO(project);
    }

    @Override
    public ProjectResponseDTO updateProjectById(Long id, ProjectUpdateDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new BusinessException("Nao e possivel atualizar um projeto inativo");
        }

        validateReferencesForUpdate(dto);
        if (dto.contractValue() != null) {
            validateContractValue(dto.contractValue());
        }

        projectMapper.updateEntityFromDTO(dto, project);
        Project updated = projectRepository.save(project);
        return projectMapper.toDTO(updated);
    }

    @Override
    public void deleteProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new BusinessException("Projeto ja esta inativo");
        }
        project.setIsActive(false);
        projectRepository.save(project);
    }

    @Override
    public ProjectResponseDTO restoreProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (Boolean.TRUE.equals(project.getIsActive())) {
            throw new BusinessException("Projeto ja esta ativo");
        }
        project.setIsActive(true);
        Project restored = projectRepository.save(project);
        return projectMapper.toDTO(restored);
    }

    @Override
    public ProjectTotalsDTO getProjectTotals(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto nao encontrado"));
        if (!Boolean.TRUE.equals(project.getIsActive())) {
            throw new ResourceNotFoundException("Projeto nao encontrado");
        }

        BigDecimal totalIncome = incomeRepository.sumIncomeByProjectId(projectId);
        BigDecimal totalExpense = expenseRepository.sumExpenseByProjectId(projectId);
        BigDecimal saldo = totalIncome.subtract(totalExpense);

        return new ProjectTotalsDTO(projectId, totalIncome, totalExpense, saldo);
    }

    @Override
    public ProjectDashboardResponseDTO getDashboard(
            ProjectStatusEnum projectStatus,
            ProjectTypeEnum projectType,
            Integer month,
            Integer year,
            String location,
            Long partnerId
    ) {
        validateMonth(month);
        validateYear(year);

        List<Project> activeProjects = projectRepository.findByIsActiveTrue();
        List<Integer> availableYears = activeProjects.stream()
                .map(this::getReferenceDate)
                .filter(Objects::nonNull)
                .map(LocalDate::getYear)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<Project> filteredProjects = activeProjects.stream()
                .filter(project -> projectStatus == null || projectStatus == project.getProjectStatus())
                .filter(project -> projectType == null || projectType == project.getProjectType())
                .filter(project -> month == null || monthMatches(project, month))
                .filter(project -> year == null || yearMatches(project, year))
                .filter(project -> partnerId == null || partnerMatches(project, partnerId))
                .filter(project -> matchesLocation(project, location))
                .toList();

        BigDecimal totalValue = sumContractValue(filteredProjects);

        List<ProjectDashboardResponseDTO.StatusMetricDTO> byStatus = Arrays.stream(ProjectStatusEnum.values())
                .map(status -> {
                    List<Project> projectsByStatus = filteredProjects.stream()
                            .filter(project -> status == project.getProjectStatus())
                            .toList();
                    return new ProjectDashboardResponseDTO.StatusMetricDTO(
                            status,
                            projectsByStatus.size(),
                            sumContractValue(projectsByStatus)
                    );
                })
                .toList();

        Map<ProjectTypeEnum, List<Project>> groupedByType = filteredProjects.stream()
                .filter(project -> project.getProjectType() != null)
                .collect(Collectors.groupingBy(Project::getProjectType));

        BigDecimal typeTotalValue = Arrays.stream(ProjectTypeEnum.values())
                .map(type -> sumContractValue(groupedByType.getOrDefault(type, List.of())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ProjectDashboardResponseDTO.TypeMetricDTO> byType = Arrays.stream(ProjectTypeEnum.values())
                .map(type -> {
                    List<Project> projectsByType = groupedByType.getOrDefault(type, List.of());
                    BigDecimal value = sumContractValue(projectsByType);
                    return new ProjectDashboardResponseDTO.TypeMetricDTO(
                            type,
                            projectsByType.size(),
                            value,
                            percentage(value, typeTotalValue)
                    );
                })
                .toList();

        List<ProjectDashboardResponseDTO.MonthMetricDTO> byMonth = IntStream.rangeClosed(1, 12)
                .mapToObj(currentMonth -> {
                    List<Project> projectsByMonth = filteredProjects.stream()
                            .filter(project -> monthMatches(project, currentMonth))
                            .toList();
                    return new ProjectDashboardResponseDTO.MonthMetricDTO(
                            currentMonth,
                            Month.of(currentMonth).getDisplayName(TextStyle.SHORT, PT_BR),
                            projectsByMonth.size(),
                            sumContractValue(projectsByMonth)
                    );
                })
                .toList();

        List<ProjectDashboardResponseDTO.LocationMetricDTO> byLocation = filteredProjects.stream()
                .collect(Collectors.groupingBy(this::toLocationKey))
                .entrySet()
                .stream()
                .map(entry -> new ProjectDashboardResponseDTO.LocationMetricDTO(
                        entry.getKey().location(),
                        entry.getKey().city(),
                        entry.getKey().state(),
                        entry.getValue().size(),
                        sumContractValue(entry.getValue())
                ))
                .sorted(Comparator
                        .comparing(ProjectDashboardResponseDTO.LocationMetricDTO::contracts, Comparator.reverseOrder())
                        .thenComparing(ProjectDashboardResponseDTO.LocationMetricDTO::totalValue, Comparator.reverseOrder())
                        .thenComparing(ProjectDashboardResponseDTO.LocationMetricDTO::location, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        List<ProjectDashboardResponseDTO.PartnerMetricDTO> byPartner = filteredProjects.stream()
                .collect(Collectors.groupingBy(this::toPrimaryPartnerKey))
                .entrySet()
                .stream()
                .map(entry -> new ProjectDashboardResponseDTO.PartnerMetricDTO(
                        entry.getKey().partnerId(),
                        entry.getKey().partnerName(),
                        entry.getValue().size(),
                        sumContractValue(entry.getValue())
                ))
                .sorted(Comparator
                        .comparing(ProjectDashboardResponseDTO.PartnerMetricDTO::contracts, Comparator.reverseOrder())
                        .thenComparing(ProjectDashboardResponseDTO.PartnerMetricDTO::totalValue, Comparator.reverseOrder())
                        .thenComparing(ProjectDashboardResponseDTO.PartnerMetricDTO::partnerName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return new ProjectDashboardResponseDTO(
                new ProjectDashboardResponseDTO.FilterDTO(
                        projectStatus,
                        projectType,
                        month,
                        year,
                        trimToNull(location),
                        partnerId
                ),
                availableYears,
                new ProjectDashboardResponseDTO.SummaryDTO(
                        filteredProjects.size(),
                        totalValue
                ),
                byStatus,
                byType,
                byMonth,
                byLocation,
                byPartner
        );
    }

    private void validatePage(int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }
    }

    private void validateMonth(Integer month) {
        if (month == null) {
            return;
        }
        if (month < 1 || month > 12) {
            throw new BusinessException("Mes deve estar entre 1 e 12");
        }
    }

    private void validateYear(Integer year) {
        if (year == null) {
            return;
        }
        if (year < 1900 || year > 3000) {
            throw new BusinessException("Ano deve estar entre 1900 e 3000");
        }
    }

    private void validateContractValue(BigDecimal contractValue) {
        if (contractValue == null) {
            return;
        }
        if (contractValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Valor do projeto deve ser maior que zero");
        }
        if (maxContractValue != null && contractValue.compareTo(maxContractValue) > 0) {
            throw new BusinessException(
                    "Valor do projeto nao pode ser maior que " + maxContractValue.toPlainString()
            );
        }
    }

    private boolean monthMatches(Project project, int month) {
        LocalDate referenceDate = getReferenceDate(project);
        return referenceDate != null && referenceDate.getMonthValue() == month;
    }

    private boolean yearMatches(Project project, int year) {
        LocalDate referenceDate = getReferenceDate(project);
        return referenceDate != null && referenceDate.getYear() == year;
    }

    private LocalDate getReferenceDate(Project project) {
        if (project.getStartDate() != null) {
            return project.getStartDate();
        }
        if (project.getOpeningDate() != null) {
            return project.getOpeningDate();
        }
        if (project.getCreatedAt() != null) {
            return project.getCreatedAt().toLocalDate();
        }
        return null;
    }

    private boolean partnerMatches(Project project, Long partnerId) {
        Long primaryPartnerId = getPartnerId(project.getPrimaryPartner());
        Long secondaryPartnerId = getPartnerId(project.getSecundaryPartner());
        return Objects.equals(primaryPartnerId, partnerId) || Objects.equals(secondaryPartnerId, partnerId);
    }

    private Long getPartnerId(Partner partner) {
        return partner == null ? null : partner.getId();
    }

    private boolean matchesLocation(Project project, String location) {
        String normalizedFilter = normalize(location);
        if (normalizedFilter.isBlank()) {
            return true;
        }

        String searchable = String.join(
                " ",
                valueOrEmpty(project.getCity()),
                valueOrEmpty(project.getState()),
                valueOrEmpty(project.getExecutionLocation()),
                resolveLocation(project)
        );

        return normalize(searchable).contains(normalizedFilter);
    }

    private BigDecimal sumContractValue(List<Project> projects) {
        return projects.stream()
                .map(Project::getContractValue)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percentage(BigDecimal value, BigDecimal total) {
        if (total == null || total.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return value.multiply(BigDecimal.valueOf(100))
                .divide(total, 2, RoundingMode.HALF_UP);
    }

    private LocationKey toLocationKey(Project project) {
        return new LocationKey(
                resolveLocation(project),
                trimToNull(project.getCity()),
                trimToNull(project.getState())
        );
    }

    private String resolveLocation(Project project) {
        String city = trimToNull(project.getCity());
        String state = trimToNull(project.getState());
        String executionLocation = trimToNull(project.getExecutionLocation());

        if (city != null && state != null) {
            return city + " - " + state;
        }
        if (city != null) {
            return city;
        }
        if (state != null) {
            return state;
        }
        if (executionLocation != null) {
            return executionLocation;
        }
        return "Nao informado";
    }

    private PartnerKey toPrimaryPartnerKey(Project project) {
        Partner partner = project.getPrimaryPartner();
        if (partner == null) {
            return new PartnerKey(null, "Nao informado");
        }
        String name = trimToNull(partner.getName());
        return new PartnerKey(partner.getId(), name != null ? name : "Nao informado");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(PT_BR).trim();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private record PartnerKey(Long partnerId, String partnerName) {
    }

    private record LocationKey(String location, String city, String state) {
    }

    private void validateReferencesForCreate(ProjectRequestDTO dto) {
        requireActivePartner(dto.primaryPartnerId(), "Parceiro primario");
        requireActivePublicAgency(dto.primaryClientId(), "Cliente primario");

        if (dto.secundaryPartnerId() != null) {
            requireActivePartner(dto.secundaryPartnerId(), "Parceiro secundario");
        }

        if (dto.secundaryClientId() != null) {
            requireActiveSecretary(dto.secundaryClientId(), "Cliente secundario");
        }

        if (dto.cordinatorId() != null) {
            requireActivePeople(dto.cordinatorId(), "Coordenador");
        }
    }

    private void validateReferencesForUpdate(ProjectUpdateDTO dto) {
        if (dto.primaryPartnerId() != null) {
            requireActivePartner(dto.primaryPartnerId(), "Parceiro primario");
        }

        if (dto.primaryClientId() != null) {
            requireActivePublicAgency(dto.primaryClientId(), "Cliente primario");
        }

        if (dto.secundaryPartnerId() != null) {
            requireActivePartner(dto.secundaryPartnerId(), "Parceiro secundario");
        }

        if (dto.secundaryClientId() != null) {
            requireActiveSecretary(dto.secundaryClientId(), "Cliente secundario");
        }

        if (dto.cordinatorId() != null) {
            requireActivePeople(dto.cordinatorId(), "Coordenador");
        }
    }

    private void requireActivePartner(Long id, String label) {
        var partner = partnerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " nao encontrado"));

        if (!Boolean.TRUE.equals(partner.getIsActive())) {
            throw new BusinessException(label + " inativo");
        }
    }

    private void requireActivePublicAgency(Long id, String label) {
        var agency = publicAgencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " nao encontrado"));

        if (!Boolean.TRUE.equals(agency.getIsActive())) {
            throw new BusinessException(label + " inativo");
        }
    }

    private void requireActiveSecretary(Long id, String label) {
        var secretary = secretaryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " nao encontrado"));

        if (!Boolean.TRUE.equals(secretary.getIsActive())) {
            throw new BusinessException(label + " inativo");
        }
    }

    private void requireActivePeople(Long id, String label) {
        var people = peopleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(label + " nao encontrado"));

        if (!Boolean.TRUE.equals(people.getIsActive())) {
            throw new BusinessException(label + " inativo");
        }
    }
}
