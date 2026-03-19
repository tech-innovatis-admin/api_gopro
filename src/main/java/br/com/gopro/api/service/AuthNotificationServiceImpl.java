package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AuthNotificationsReadResponseDTO;
import br.com.gopro.api.dtos.AuthNotificationResponseDTO;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.ProjectStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.AuditLog;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.AuditLogRepository;
import br.com.gopro.api.repository.ProjectRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthNotificationServiceImpl implements AuthNotificationService {

    private static final String CATEGORY_CREATED = "CREATED";
    private static final String CATEGORY_STATUS_CHANGE = "STATUS_CHANGE";
    private static final String CATEGORY_EXPIRING = "EXPIRING";

    private static final String SEVERITY_INFO = "INFO";
    private static final String SEVERITY_DANGER = "DANGER";

    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;
    private static final int CREATED_WINDOW_DAYS = 30;
    private static final int EXPIRING_WINDOW_DAYS = 90;
    private static final int MIN_AUDIT_SCAN_SIZE = 80;
    private static final int MAX_AUDIT_SCAN_SIZE = 500;
    private static final int STATUS_SCAN_MULTIPLIER = 15;

    private static final Set<String> PROJECT_STATUS_VALUES = Arrays.stream(ProjectStatusEnum.values())
            .map(Enum::name)
            .collect(Collectors.toUnmodifiableSet());

    private static final Map<String, String> STATUS_LABELS = Map.of(
            "PRE_PROJETO", "Pre-projeto",
            "PLANEJAMENTO", "Planejamento",
            "EXECUCAO", "Execucao",
            "FINALIZADO", "Finalizado",
            "SUSPENSO", "Suspenso"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();

    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<AuthNotificationResponseDTO> listNotifications(AuthenticatedUserPrincipal principal, int size) {
        if (principal == null || principal.id() == null) {
            throw new UnauthorizedException("Usuario autenticado invalido");
        }

        validateSize(size);
        AppUser user = loadUser(principal.id());
        OffsetDateTime notificationsLastReadAt = toNullableOffsetDateTime(user.getNotificationsLastReadAt());

        List<AuthNotificationResponseDTO> merged = new ArrayList<>();
        merged.addAll(buildExpiringNotifications(size));
        merged.addAll(buildStatusChangeNotifications(size));
        merged.addAll(buildCreatedNotifications(size));

        return deduplicateById(merged).stream()
                .sorted(this::compareNotifications)
                .filter(notification -> isUnread(notification, notificationsLastReadAt))
                .limit(size)
                .toList();
    }

    @Override
    @Transactional
    public AuthNotificationsReadResponseDTO markAllAsRead(AuthenticatedUserPrincipal principal) {
        if (principal == null || principal.id() == null) {
            throw new UnauthorizedException("Usuario autenticado invalido");
        }

        AppUser user = loadUser(principal.id());
        LocalDateTime now = LocalDateTime.now(SYSTEM_ZONE);
        user.setNotificationsLastReadAt(now);
        user.setUpdatedBy(principal.id());
        appUserRepository.save(user);

        return new AuthNotificationsReadResponseDTO(now);
    }

    private void validateSize(int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new BusinessException("Tamanho da lista de notificacoes deve estar entre 1 e 100");
        }
    }

    private List<AuthNotificationResponseDTO> buildCreatedNotifications(int requestedSize) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(CREATED_WINDOW_DAYS);
        int fetchSize = Math.min(MAX_SIZE, Math.max(requestedSize, MIN_SIZE) * 2);

        List<Project> projects = projectRepository.findRecentCreatedProjects(
                cutoff,
                PageRequest.of(0, fetchSize)
        );

        return projects.stream()
                .filter(project -> project.getCreatedAt() != null)
                .map(project -> {
                    String contractLabel = buildContractLabel(project, project.getId());
                    String message = contractLabel + " criado em " + DATE_FORMATTER.format(project.getCreatedAt().toLocalDate()) + ".";
                    return new AuthNotificationResponseDTO(
                            "created-" + project.getId(),
                            CATEGORY_CREATED,
                            SEVERITY_INFO,
                            "Novo contrato criado",
                            message,
                            buildContractHref(project.getId()),
                            project.getId(),
                            toOffsetDateTime(project.getCreatedAt())
                    );
                })
                .toList();
    }

    private List<AuthNotificationResponseDTO> buildExpiringNotifications(int requestedSize) {
        LocalDate referenceDate = LocalDate.now(SYSTEM_ZONE);
        LocalDate limitDate = referenceDate.plusDays(EXPIRING_WINDOW_DAYS);
        int fetchSize = Math.min(MAX_SIZE, Math.max(requestedSize, MIN_SIZE) * 2);

        List<Project> projects = projectRepository.findExpiringProjectsBetween(
                referenceDate,
                limitDate,
                PageRequest.of(0, fetchSize)
        );

        return projects.stream()
                .filter(project -> project.getEndDate() != null)
                .map(project -> {
                    long daysToExpiration = ChronoUnit.DAYS.between(referenceDate, project.getEndDate());
                    String countdownText = countdownText(daysToExpiration);
                    String contractLabel = buildContractLabel(project, project.getId());
                    String message = contractLabel + " " + countdownText + " (" + DATE_FORMATTER.format(project.getEndDate()) + ").";

                    return new AuthNotificationResponseDTO(
                            "expiring-" + project.getId(),
                            CATEGORY_EXPIRING,
                            SEVERITY_DANGER,
                            "Contrato perto do vencimento",
                            message,
                            buildContractHref(project.getId()),
                            project.getId(),
                            project.getEndDate().atStartOfDay(SYSTEM_ZONE).toOffsetDateTime()
                    );
                })
                .toList();
    }

    private List<AuthNotificationResponseDTO> buildStatusChangeNotifications(int requestedSize) {
        int scanLimit = Math.min(
                MAX_AUDIT_SCAN_SIZE,
                Math.max(MIN_AUDIT_SCAN_SIZE, requestedSize * STATUS_SCAN_MULTIPLIER)
        );

        List<AuditLog> logs = auditLogRepository.findRecentByScopeAndAction(
                AuditScopeEnum.CONTRACTS,
                "ATUALIZAR",
                PageRequest.of(0, scanLimit)
        );

        List<StatusChangeEvent> statusEvents = logs.stream()
                .map(this::toStatusChangeEvent)
                .flatMap(Optional::stream)
                .toList();

        Set<Long> projectIds = statusEvents.stream()
                .map(StatusChangeEvent::contractId)
                .collect(Collectors.toSet());

        Map<Long, Project> projectsById = projectRepository.findAllById(projectIds).stream()
                .collect(Collectors.toMap(Project::getId, project -> project));

        return statusEvents.stream()
                .map(event -> {
                    Project project = projectsById.get(event.contractId());
                    String contractLabel = buildContractLabel(project, event.contractId());
                    String fromLabel = resolveStatusLabel(event.fromStatus());
                    String toLabel = resolveStatusLabel(event.toStatus());
                    String message = contractLabel + " mudou de " + fromLabel + " para " + toLabel + ".";

                    return new AuthNotificationResponseDTO(
                            event.notificationId(),
                            CATEGORY_STATUS_CHANGE,
                            SEVERITY_INFO,
                            "Mudanca de status de contrato",
                            message,
                            buildContractHref(event.contractId()),
                            event.contractId(),
                            event.occurredAt()
                    );
                })
                .toList();
    }

    private Optional<StatusChangeEvent> toStatusChangeEvent(AuditLog log) {
        if (log == null || !isContractEntity(log)) {
            return Optional.empty();
        }

        String fromStatus = extractProjectStatus(log.getBeforeJson());
        String toStatus = extractProjectStatus(log.getAfterJson());

        if (fromStatus == null || toStatus == null || fromStatus.equals(toStatus)) {
            return Optional.empty();
        }

        Long contractId = resolveContractId(log);
        if (contractId == null) {
            return Optional.empty();
        }

        String idToken = trimToNull(log.getAuditId());
        String notificationId = idToken != null ? "status-" + idToken : "status-" + log.getId();

        return Optional.of(new StatusChangeEvent(
                notificationId,
                contractId,
                fromStatus,
                toStatus,
                resolveOccurredAt(log)
        ));
    }

    private boolean isContractEntity(AuditLog log) {
        String entityType = safeLower(log.getEntityType());
        String entityName = safeLower(log.getEntidadePrincipal());
        return entityType.contains("project")
                || entityType.contains("contract")
                || entityType.contains("contrato")
                || entityName.contains("project")
                || entityName.contains("projeto")
                || entityName.contains("contrato");
    }

    private String extractProjectStatus(String json) {
        JsonNode root = parseJsonNode(json);
        if (root == null || !root.isObject()) {
            return null;
        }

        String status = normalizeStatus(readTextValue(root, "projectStatus"));
        if (status == null) {
            status = normalizeStatus(readTextValue(root, "status"));
        }

        if (status == null || !PROJECT_STATUS_VALUES.contains(status)) {
            return null;
        }
        return status;
    }

    private String normalizeStatus(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        return rawValue.trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private Long resolveContractId(AuditLog log) {
        Long entityId = parsePositiveLong(log.getEntityId());
        if (entityId != null) {
            return entityId;
        }

        JsonNode detailsNode = parseJsonNode(log.getDetalhesTecnicosJson());
        Long contractId = readPositiveLong(detailsNode, "contractId");
        if (contractId != null) {
            return contractId;
        }
        return readPositiveLong(detailsNode, "projectId");
    }

    private Long readPositiveLong(JsonNode root, String field) {
        if (root == null || field == null || field.isBlank()) {
            return null;
        }
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return null;
        }

        if (node.isIntegralNumber()) {
            long value = node.longValue();
            return value > 0 ? value : null;
        }

        if (node.isTextual()) {
            return parsePositiveLong(node.asText());
        }

        return null;
    }

    private String readTextValue(JsonNode root, String field) {
        if (root == null || field == null || field.isBlank()) {
            return null;
        }
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText(null);
        return value == null || value.isBlank() ? null : value;
    }

    private JsonNode parseJsonNode(String json) {
        String normalized = trimToNull(json);
        if (normalized == null) {
            return null;
        }
        try {
            return objectMapper.readTree(normalized);
        } catch (Exception ignored) {
            return null;
        }
    }

    private OffsetDateTime resolveOccurredAt(AuditLog log) {
        if (log.getEventAt() != null) {
            return log.getEventAt();
        }
        if (log.getCreatedAt() != null) {
            return toOffsetDateTime(log.getCreatedAt());
        }
        return OffsetDateTime.now();
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return OffsetDateTime.now();
        }
        return value.atZone(SYSTEM_ZONE).toOffsetDateTime();
    }

    private OffsetDateTime toNullableOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        return value.atZone(SYSTEM_ZONE).toOffsetDateTime();
    }

    private String countdownText(long daysToExpiration) {
        if (daysToExpiration <= 0) {
            return "vence hoje";
        }
        if (daysToExpiration == 1) {
            return "vence amanha";
        }
        return "vence em " + daysToExpiration + " dias";
    }

    private String resolveStatusLabel(String statusValue) {
        return STATUS_LABELS.getOrDefault(statusValue, statusValue);
    }

    private String buildContractHref(Long contractId) {
        if (contractId == null) {
            return "/contratos";
        }
        return "/contratos/" + contractId;
    }

    private String buildContractLabel(Project project, Long fallbackContractId) {
        String code = project == null ? null : trimToNull(project.getCode());
        String name = project == null ? null : trimToNull(project.getName());

        if (code != null && name != null) {
            return "Contrato " + code + " (" + name + ")";
        }
        if (code != null) {
            return "Contrato " + code;
        }
        if (name != null) {
            return "Contrato " + name;
        }
        if (fallbackContractId != null) {
            return "Contrato #" + fallbackContractId;
        }
        return "Contrato";
    }

    private List<AuthNotificationResponseDTO> deduplicateById(List<AuthNotificationResponseDTO> items) {
        Map<String, AuthNotificationResponseDTO> deduplicated = new LinkedHashMap<>();
        for (AuthNotificationResponseDTO item : items) {
            if (item == null) {
                continue;
            }
            String id = trimToNull(item.id());
            if (id == null) {
                continue;
            }
            deduplicated.putIfAbsent(id, item);
        }
        return new ArrayList<>(deduplicated.values());
    }

    private boolean isUnread(AuthNotificationResponseDTO notification, OffsetDateTime readAt) {
        if (readAt == null) {
            return true;
        }

        OffsetDateTime occurredAt = notification.occurredAt();
        if (occurredAt == null) {
            return true;
        }

        return occurredAt.isAfter(readAt);
    }

    private int compareNotifications(AuthNotificationResponseDTO first, AuthNotificationResponseDTO second) {
        int firstPriority = categoryPriority(first.category());
        int secondPriority = categoryPriority(second.category());

        if (firstPriority != secondPriority) {
            return Integer.compare(firstPriority, secondPriority);
        }

        if (CATEGORY_EXPIRING.equals(first.category()) && CATEGORY_EXPIRING.equals(second.category())) {
            return compareOffsetDateTimeAscending(first.occurredAt(), second.occurredAt());
        }

        return compareOffsetDateTimeDescending(first.occurredAt(), second.occurredAt());
    }

    private int categoryPriority(String category) {
        if (CATEGORY_EXPIRING.equals(category)) {
            return 0;
        }
        if (CATEGORY_STATUS_CHANGE.equals(category)) {
            return 1;
        }
        return 2;
    }

    private int compareOffsetDateTimeAscending(OffsetDateTime first, OffsetDateTime second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return first.compareTo(second);
    }

    private int compareOffsetDateTimeDescending(OffsetDateTime first, OffsetDateTime second) {
        if (first == null && second == null) {
            return 0;
        }
        if (first == null) {
            return 1;
        }
        if (second == null) {
            return -1;
        }
        return second.compareTo(first);
    }

    private AppUser loadUser(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }

    private Long parsePositiveLong(String rawValue) {
        String normalized = trimToNull(rawValue);
        if (normalized == null) {
            return null;
        }
        try {
            long parsed = Long.parseLong(normalized);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private record StatusChangeEvent(
            String notificationId,
            Long contractId,
            String fromStatus,
            String toStatus,
            OffsetDateTime occurredAt
    ) {
    }
}
