package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.ProjectDashboardResponseDTO;
import br.com.gopro.api.dtos.ProjectRequestDTO;
import br.com.gopro.api.dtos.ProjectResponseDTO;
import br.com.gopro.api.dtos.ProjectTotalsDTO;
import br.com.gopro.api.dtos.ProjectUpdateDTO;
import br.com.gopro.api.enums.ProjectGovIfEnum;
import br.com.gopro.api.enums.RoleProjectPeopleEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.enums.StatusProjectPeopleEnum;
import br.com.gopro.api.enums.ProjectTypeEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.mapper.ProjectMapper;
import br.com.gopro.api.model.Partner;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.model.ProjectPeople;
import br.com.gopro.api.repository.ExpenseRepository;
import br.com.gopro.api.repository.IncomeRepository;
import br.com.gopro.api.repository.PartnerRepository;
import br.com.gopro.api.repository.PeopleRepository;
import br.com.gopro.api.repository.ProjectPeopleRepository;
import br.com.gopro.api.repository.ProjectRepository;
import br.com.gopro.api.repository.PublicAgencyRepository;
import br.com.gopro.api.repository.SecretaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final ProjectPeopleRepository projectPeopleRepository;
    private static final Locale PT_BR = new Locale("pt", "BR");
    private static final int CODE_SEQUENCE_DIGITS = 4;
    private static final int MAX_CODE_GENERATION_ATTEMPTS = 5;
    private static final int DUPLICATE_CREATE_GUARD_WINDOW_SECONDS = 15;
    private static final int DUPLICATE_CREATE_GUARD_MAX_RESULTS = 20;
    private static final Pattern UF_PATTERN = Pattern.compile("^[A-Z]{2}$");
    @Value("${app.project.max-contract-value:9999999999999.99}")
    private BigDecimal maxContractValue;

    @Override
    @Transactional
    public ProjectResponseDTO createProject(ProjectRequestDTO dto) {
        validateContractValue(dto.contractValue());
        validateReferencesForCreate(dto);
        String normalizedUf = normalizeUf(dto.state());
        Long auditUserId = resolveAuditUserId(dto.createdBy(), null);

        Project recentDuplicate = findRecentDuplicateProject(dto, normalizedUf, auditUserId);
        if (recentDuplicate != null) {
            return projectMapper.toDTO(recentDuplicate);
        }

        for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
            Project project = projectMapper.toEntity(dto);
            project.setState(normalizedUf);
            project.setExecutedByInnovatis(Boolean.TRUE.equals(dto.executedByInnovatis()));
            project.setCreatedBy(auditUserId);
            project.setCode(generateContractCode(project.getProjectType(), project.getProjectGovIf(), normalizedUf));
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

            try {
                Project saved = projectRepository.saveAndFlush(project);
                ensureCoordinatorLinkedToProjectPeople(saved, auditUserId, null);
                return projectMapper.toDTO(saved);
            } catch (DataIntegrityViolationException exception) {
                if (isProjectCodeConflict(exception) && attempt < MAX_CODE_GENERATION_ATTEMPTS - 1) {
                    continue;
                }
                throw exception;
            }
        }

        throw new BusinessException("Nao foi possivel gerar codigo unico para o contrato");
    }

    @Override
    public PageResponseDTO<ProjectResponseDTO> listAllProjects(int page, int size) {
        validatePage(page, size);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id"))
        );
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
    @Transactional
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
        if (dto.executedByInnovatis() != null) {
            project.setExecutedByInnovatis(dto.executedByInnovatis());
        }
        Project updated = projectRepository.save(project);
        ensureCoordinatorLinkedToProjectPeople(updated, dto.updatedBy(), project.getCreatedBy());
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
            ProjectGovIfEnum projectGovIf,
            Boolean executedByInnovatis,
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
                .filter(project -> projectGovIf == null || projectGovIf == project.getProjectGovIf())
                .filter(project -> executedByInnovatis == null || executedByInnovatis.equals(project.getExecutedByInnovatis()))
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
                        entry.getKey().partnerAcronym(),
                        entry.getKey().partnerName(),
                        entry.getValue().size(),
                        sumContractValue(entry.getValue())
                ))
                .sorted(Comparator
                        .comparing(ProjectDashboardResponseDTO.PartnerMetricDTO::contracts, Comparator.reverseOrder())
                        .thenComparing(ProjectDashboardResponseDTO.PartnerMetricDTO::totalValue, Comparator.reverseOrder())
                        .thenComparing(ProjectDashboardResponseDTO.PartnerMetricDTO::partnerName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        ProjectDashboardResponseDTO.ExpiringContractsDTO expiringContracts = buildExpiringContracts(filteredProjects);

        return new ProjectDashboardResponseDTO(
                new ProjectDashboardResponseDTO.FilterDTO(
                        projectStatus,
                        projectType,
                        projectGovIf,
                        executedByInnovatis,
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
                byPartner,
                expiringContracts
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

    private String generateContractCode(ProjectTypeEnum projectType, ProjectGovIfEnum projectGovIf, String state) {
        String typeCode = toCodeType(projectType);
        String govIfCode = toCodeGovIf(projectGovIf);
        String uf = normalizeUf(state);
        int year = LocalDate.now().getYear();
        String prefix = typeCode + govIfCode + "/" + uf + "-" + year;
        int sequence = resolveNextSequence(prefix);

        return prefix + formatSequence(sequence);
    }

    private String toCodeType(ProjectTypeEnum projectType) {
        if (projectType == null) {
            throw new BusinessException("Tipo do contrato e obrigatorio");
        }
        return switch (projectType) {
            case PROJETO -> "PROJ";
            case PRODUTO -> "PROD";
        };
    }

    private String toCodeGovIf(ProjectGovIfEnum projectGovIf) {
        if (projectGovIf == null) {
            throw new BusinessException("Unidade GOV/IF e obrigatoria");
        }
        return switch (projectGovIf) {
            case GOV -> "GOV";
            case IF -> "IF";
        };
    }

    private String normalizeUf(String state) {
        String uf = trimToNull(state);
        if (uf == null) {
            throw new BusinessException("UF e obrigatoria");
        }

        String normalizedUf = stripAccents(uf)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z]", "");

        if (!UF_PATTERN.matcher(normalizedUf).matches()) {
            throw new BusinessException("UF deve possuir 2 letras (ex: SP)");
        }
        return normalizedUf;
    }

    private int resolveNextSequence(String codePrefix) {
        Pattern pattern = Pattern.compile("^" + Pattern.quote(codePrefix) + "(\\d{" + CODE_SEQUENCE_DIGITS + "})$");

        int currentMax = projectRepository.findCodesByPrefix(codePrefix).stream()
                .map(pattern::matcher)
                .filter(Matcher::matches)
                .mapToInt(matcher -> Integer.parseInt(matcher.group(1)))
                .max()
                .orElse(0);

        int maxSequence = (int) Math.pow(10, CODE_SEQUENCE_DIGITS) - 1;
        int next = currentMax + 1;
        if (next > maxSequence) {
            throw new BusinessException("Limite de sequencial anual atingido para tipo e UF informados");
        }
        return next;
    }

    private String formatSequence(int sequence) {
        return String.format(Locale.ROOT, "%0" + CODE_SEQUENCE_DIGITS + "d", sequence);
    }

    private Project findRecentDuplicateProject(ProjectRequestDTO dto, String normalizedUf, Long auditUserId) {
        LocalDateTime fromDate = LocalDateTime.now().minusSeconds(DUPLICATE_CREATE_GUARD_WINDOW_SECONDS);
        Pageable pageable = PageRequest.of(0, DUPLICATE_CREATE_GUARD_MAX_RESULTS);

        return projectRepository.findRecentCreatedProjects(fromDate, pageable).stream()
                .filter(project -> Boolean.TRUE.equals(project.getIsActive()))
                .filter(project -> auditUserId == null || Objects.equals(project.getCreatedBy(), auditUserId))
                .filter(project -> matchesCreateRequest(project, dto, normalizedUf))
                .findFirst()
                .orElse(null);
    }

    private boolean matchesCreateRequest(Project project, ProjectRequestDTO dto, String normalizedUf) {
        return Objects.equals(trimToNull(project.getName()), trimToNull(dto.name()))
                && project.getProjectStatus() == dto.projectStatus()
                && Objects.equals(trimToNull(project.getObject()), trimToNull(dto.object()))
                && Objects.equals(getId(project.getPrimaryPartner()), dto.primaryPartnerId())
                && Objects.equals(getId(project.getSecundaryPartner()), dto.secundaryPartnerId())
                && Objects.equals(getId(project.getPrimaryClient()), dto.primaryClientId())
                && Objects.equals(getId(project.getSecundaryClient()), dto.secundaryClientId())
                && Objects.equals(getId(project.getCordinator()), dto.cordinatorId())
                && project.getProjectGovIf() == dto.projectGovIf()
                && project.getProjectType() == dto.projectType()
                && sameBigDecimal(project.getContractValue(), dto.contractValue())
                && Objects.equals(project.getStartDate(), dto.startDate())
                && Objects.equals(project.getEndDate(), dto.endDate())
                && Objects.equals(project.getOpeningDate(), dto.openingDate())
                && Objects.equals(project.getClosingDate(), dto.closingDate())
                && Objects.equals(trimToNull(project.getCity()), trimToNull(dto.city()))
                && Objects.equals(trimToNull(project.getExecutionLocation()), trimToNull(dto.executionLocation()))
                && Objects.equals(normalizeUf(project.getState()), normalizedUf)
                && Objects.equals(project.getExecutedByInnovatis(), dto.executedByInnovatis());
    }

    private Long getId(Object entity) {
        if (entity instanceof Partner partner) {
            return partner.getId();
        }
        if (entity instanceof br.com.gopro.api.model.PublicAgency publicAgency) {
            return publicAgency.getId();
        }
        if (entity instanceof br.com.gopro.api.model.Secretary secretary) {
            return secretary.getId();
        }
        if (entity instanceof br.com.gopro.api.model.People people) {
            return people.getId();
        }
        return null;
    }

    private boolean sameBigDecimal(BigDecimal left, BigDecimal right) {
        if (left == null || right == null) {
            return left == null && right == null;
        }
        return left.compareTo(right) == 0;
    }

    private boolean isProjectCodeConflict(DataIntegrityViolationException exception) {
        String details = dataIntegrityDetails(exception);
        return details.contains("projects_code_key")
                || details.contains("project_code_key")
                || (details.contains("duplicate key") && details.contains("code"))
                || (details.contains("unique") && details.contains("code"));
    }

    private String dataIntegrityDetails(DataIntegrityViolationException exception) {
        if (exception.getMostSpecificCause() != null && exception.getMostSpecificCause().getMessage() != null) {
            return exception.getMostSpecificCause().getMessage().toLowerCase(Locale.ROOT);
        }
        if (exception.getMessage() != null) {
            return exception.getMessage().toLowerCase(Locale.ROOT);
        }
        return "";
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
            return new PartnerKey(null, null, "Nao informado");
        }
        String acronym = trimToNull(partner.getAcronym());
        String name = trimToNull(partner.getName());
        return new PartnerKey(partner.getId(), acronym, name != null ? name : "Nao informado");
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return stripAccents(value).toLowerCase(PT_BR).trim();
    }

    private String stripAccents(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
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

    private ProjectDashboardResponseDTO.ExpiringContractsDTO buildExpiringContracts(List<Project> projects) {
        LocalDate referenceDate = LocalDate.now();
        LocalDate oneMonthLimit = referenceDate.plusMonths(1);
        LocalDate threeMonthLimit = referenceDate.plusMonths(3);
        LocalDate sixMonthLimit = referenceDate.plusMonths(6);
        LocalDate oneYearLimit = referenceDate.plusYears(1);

        List<Project> upcomingProjects = projects.stream()
                .filter(project -> project.getEndDate() != null)
                .filter(project -> isWithinDateRange(project.getEndDate(), referenceDate, oneYearLimit))
                .sorted(Comparator
                        .comparing(Project::getEndDate)
                        .thenComparing(Project::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();

        long upToOneMonth = upcomingProjects.stream()
                .filter(project -> !project.getEndDate().isAfter(oneMonthLimit))
                .count();

        long upToThreeMonths = upcomingProjects.stream()
                .filter(project -> project.getEndDate().isAfter(oneMonthLimit))
                .filter(project -> !project.getEndDate().isAfter(threeMonthLimit))
                .count();

        long upToSixMonths = upcomingProjects.stream()
                .filter(project -> project.getEndDate().isAfter(threeMonthLimit))
                .filter(project -> !project.getEndDate().isAfter(sixMonthLimit))
                .count();

        long upToOneYear = upcomingProjects.stream()
                .filter(project -> project.getEndDate().isAfter(sixMonthLimit))
                .filter(project -> !project.getEndDate().isAfter(oneYearLimit))
                .count();

        List<ProjectDashboardResponseDTO.ExpiringContractDTO> contracts = upcomingProjects.stream()
                .map(project -> new ProjectDashboardResponseDTO.ExpiringContractDTO(
                        project.getId(),
                        project.getName(),
                        project.getCode(),
                        resolvePrimaryClientName(project),
                        project.getEndDate(),
                        ChronoUnit.DAYS.between(referenceDate, project.getEndDate()),
                        project.getProjectStatus(),
                        project.getContractValue()
                ))
                .toList();

        return new ProjectDashboardResponseDTO.ExpiringContractsDTO(
                referenceDate,
                upToOneMonth,
                upToThreeMonths,
                upToSixMonths,
                upToOneYear,
                contracts
        );
    }

    private boolean isWithinDateRange(LocalDate value, LocalDate startInclusive, LocalDate endInclusive) {
        return value != null
                && !value.isBefore(startInclusive)
                && !value.isAfter(endInclusive);
    }

    private String resolvePrimaryClientName(Project project) {
        if (project == null || project.getPrimaryClient() == null) {
            return "Nao informado";
        }
        String name = trimToNull(project.getPrimaryClient().getName());
        return name != null ? name : "Nao informado";
    }

    private record PartnerKey(Long partnerId, String partnerAcronym, String partnerName) {
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

    private void ensureCoordinatorLinkedToProjectPeople(Project project, Long actorId, Long fallbackCreatedBy) {
        if (project.getId() == null || project.getCordinator() == null || project.getCordinator().getId() == null) {
            return;
        }

        Long projectId = project.getId();
        Long personId = project.getCordinator().getId();

        if (projectPeopleRepository.existsByProject_IdAndPerson_IdAndIsActiveTrue(projectId, personId)) {
            return;
        }

        ProjectPeople projectPeople = projectPeopleRepository
                .findFirstByProject_IdAndPerson_Id(projectId, personId)
                .orElseGet(ProjectPeople::new);

        projectPeople.setProject(project);
        projectPeople.setPerson(project.getCordinator());
        projectPeople.setRole(RoleProjectPeopleEnum.DIRETOR);
        projectPeople.setStatus(StatusProjectPeopleEnum.ATIVO);
        projectPeople.setStartDate(firstNonNullDate(project.getStartDate(), project.getOpeningDate()));
        projectPeople.setIsActive(true);

        Long auditUserId = resolveAuditUserId(actorId, fallbackCreatedBy);
        if (projectPeople.getCreatedBy() == null) {
            projectPeople.setCreatedBy(auditUserId);
        }
        projectPeople.setUpdatedBy(auditUserId);

        projectPeopleRepository.save(projectPeople);
    }

    private Long resolveAuditUserId(Long preferredUserId, Long fallbackUserId) {
        if (preferredUserId != null) {
            return preferredUserId;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUserPrincipal principal) {
            if (principal.id() != null) {
                return principal.id();
            }
        }
        if (fallbackUserId != null) {
            return fallbackUserId;
        }
        return null;
    }

    private LocalDate firstNonNullDate(LocalDate first, LocalDate second) {
        if (first != null) {
            return first;
        }
        return second;
    }
}
