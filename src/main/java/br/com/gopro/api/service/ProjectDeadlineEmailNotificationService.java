package br.com.gopro.api.service;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.EmailNotificationDispatch;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.EmailNotificationDispatchRepository;
import br.com.gopro.api.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProjectDeadlineEmailNotificationService {

    private static final String NOTIFICATION_TYPE = "PROJECT_DEADLINE";
    private static final String ENTITY_TYPE = "PROJECT";
    private static final LocalTime DEFAULT_DISPATCH_TIME = LocalTime.of(9, 0);
    private static final Set<UserRoleEnum> DEFAULT_ROLES = Set.of(
            UserRoleEnum.SUPERADMIN,
            UserRoleEnum.ADMIN,
            UserRoleEnum.OWNER
    );

    private final ProjectRepository projectRepository;
    private final AppUserRepository appUserRepository;
    private final EmailNotificationDispatchRepository dispatchRepository;
    private final EmailService emailService;

    @Value("${app.notifications.email.project-deadline.enabled:false}")
    private boolean enabled;

    @Value("${app.notifications.email.project-deadline.days-before:30,7,1}")
    private String daysBeforeProperty;

    @Value("${app.notifications.email.project-deadline.recipient-roles:SUPERADMIN,ADMIN,OWNER}")
    private String recipientRolesProperty;

    @Value("${app.notifications.email.project-deadline.zone:America/Sao_Paulo}")
    private String zoneIdProperty;

    @Value("${app.notifications.email.project-deadline.batch-size:200}")
    private int batchSize;

    @Scheduled(
            cron = "${app.notifications.email.project-deadline.cron:0 0 9 * * *}",
            zone = "${app.notifications.email.project-deadline.zone:America/Sao_Paulo}"
    )
    @Transactional
    public void notifyScheduledDeadlines() {
        if (!enabled) {
            return;
        }
        dispatchProjectDeadlines(LocalDate.now(resolveZoneId()), false);
    }

    @Transactional
    public ProjectDeadlineDispatchSummary triggerManual(LocalDate referenceDate) {
        return dispatchProjectDeadlines(referenceDate, true);
    }

    @Transactional
    ProjectDeadlineDispatchSummary notifyProjectDeadlines(LocalDate referenceDate) {
        return dispatchProjectDeadlines(referenceDate, false);
    }

    private ProjectDeadlineDispatchSummary dispatchProjectDeadlines(LocalDate referenceDate, boolean manualTrigger) {
        if (!manualTrigger && !enabled) {
            return new ProjectDeadlineDispatchSummary(referenceDate, 0, 0, 0, 0, false, "Notificacoes por email desabilitadas");
        }

        List<Integer> daysBefore = resolveDaysBefore();
        if (daysBefore.isEmpty()) {
            log.warn("nenhum marco de prazo configurado para notificacoes por email");
            return new ProjectDeadlineDispatchSummary(referenceDate, 0, 0, 0, 0, enabled, "Nenhum marco de prazo configurado");
        }

        List<AppUser> recipients = loadRecipients();
        if (recipients.isEmpty()) {
            log.info("nenhum destinatario elegivel para notificacoes de prazo");
            return new ProjectDeadlineDispatchSummary(referenceDate, 0, 0, 0, 0, enabled, "Nenhum destinatario elegivel");
        }

        int maxDaysBefore = daysBefore.stream().max(Integer::compareTo).orElse(0);
        List<Project> projects = loadProjects(referenceDate, maxDaysBefore);
        int emailsSent = 0;
        int emailsSkipped = 0;

        for (Project project : projects) {
            if (project.getId() == null || project.getEndDate() == null) {
                emailsSkipped++;
                continue;
            }

            long daysRemaining = ChronoUnit.DAYS.between(referenceDate, project.getEndDate());
            List<LocalDate> pendingMilestoneDates = resolvePendingMilestoneDates(project, referenceDate, daysBefore);
            if (pendingMilestoneDates.isEmpty()) {
                emailsSkipped++;
                continue;
            }

            for (AppUser recipient : recipients) {
                String email = normalizeEmail(recipient.getEmail());
                if (email == null) {
                    emailsSkipped++;
                    continue;
                }

                for (LocalDate milestoneReferenceDate : pendingMilestoneDates) {
                    if (alreadySent(project.getId(), email, milestoneReferenceDate)) {
                        emailsSkipped++;
                        continue;
                    }

                    EmailService.EmailDispatchResult result = emailService.sendProjectDeadlineNotification(
                            email,
                            recipient.getFullName(),
                            project,
                            daysRemaining
                    );

                    if (!result.success()) {
                        emailsSkipped++;
                        continue;
                    }

                    if (saveDispatch(project.getId(), email, milestoneReferenceDate)) {
                        emailsSent++;
                    } else {
                        emailsSkipped++;
                    }
                }
            }
        }

        return new ProjectDeadlineDispatchSummary(
                referenceDate,
                projects.size(),
                recipients.size(),
                emailsSent,
                emailsSkipped,
                enabled,
                emailsSent > 0 ? "Disparo concluido" : "Nenhum email enviado"
        );
    }

    private boolean alreadySent(Long projectId, String recipientEmail, LocalDate referenceDate) {
        return dispatchRepository.existsByNotificationTypeAndEntityTypeAndEntityIdAndRecipientEmailIgnoreCaseAndReferenceDate(
                NOTIFICATION_TYPE,
                ENTITY_TYPE,
                projectId,
                recipientEmail,
                referenceDate
        );
    }

    private boolean saveDispatch(Long projectId, String recipientEmail, LocalDate referenceDate) {
        try {
            EmailNotificationDispatch dispatch = new EmailNotificationDispatch();
            dispatch.setNotificationType(NOTIFICATION_TYPE);
            dispatch.setEntityType(ENTITY_TYPE);
            dispatch.setEntityId(projectId);
            dispatch.setRecipientEmail(recipientEmail);
            dispatch.setReferenceDate(referenceDate);
            dispatchRepository.saveAndFlush(dispatch);
            return true;
        } catch (DataIntegrityViolationException ex) {
            log.warn(
                    "dispatch duplicado ignorado notificationType={} entityId={} recipientEmail={} referenceDate={}",
                    NOTIFICATION_TYPE,
                    projectId,
                    recipientEmail,
                    referenceDate
            );
            return false;
        }
    }

    private List<Project> loadProjects(LocalDate referenceDate, int maxDaysBefore) {
        int pageSize = Math.max(1, batchSize);
        int page = 0;
        List<Project> projects = new java.util.ArrayList<>();

        while (true) {
            List<Project> chunk = projectRepository.findExpiringProjectsBetween(
                    referenceDate,
                    referenceDate.plusDays(maxDaysBefore),
                    PageRequest.of(page, pageSize)
            );
            if (chunk.isEmpty()) {
                break;
            }
            projects.addAll(chunk);
            if (chunk.size() < pageSize) {
                break;
            }
            page++;
        }

        return projects;
    }

    private List<LocalDate> resolvePendingMilestoneDates(Project project, LocalDate referenceDate, List<Integer> daysBefore) {
        if (project == null || project.getEndDate() == null || referenceDate == null || daysBefore == null || daysBefore.isEmpty()) {
            return List.of();
        }

        LocalDate endDate = project.getEndDate();
        long daysRemaining = ChronoUnit.DAYS.between(referenceDate, endDate);
        if (daysRemaining < 0) {
            return List.of();
        }

        return daysBefore.stream()
                .filter(dayBefore -> isMilestonePending(project, referenceDate, dayBefore, daysRemaining))
                .map(endDate::minusDays)
                .filter(milestoneReferenceDate -> !milestoneReferenceDate.isAfter(referenceDate))
                .sorted()
                .toList();
    }

    private boolean isMilestonePending(Project project, LocalDate referenceDate, int dayBefore, long daysRemaining) {
        if (daysRemaining == dayBefore) {
            return true;
        }
        if (daysRemaining > dayBefore) {
            return false;
        }
        return wasCreatedAfterDispatchWindow(project.getCreatedAt(), project.getEndDate().minusDays(dayBefore), referenceDate);
    }

    private boolean wasCreatedAfterDispatchWindow(LocalDateTime createdAt, LocalDate milestoneReferenceDate, LocalDate referenceDate) {
        if (createdAt == null || milestoneReferenceDate == null || referenceDate == null) {
            return false;
        }
        if (!milestoneReferenceDate.isBefore(referenceDate)) {
            return false;
        }

        LocalDateTime scheduledDispatchAt = milestoneReferenceDate.atTime(DEFAULT_DISPATCH_TIME);
        return !createdAt.isBefore(scheduledDispatchAt);
    }

    private List<AppUser> loadRecipients() {
        Set<UserRoleEnum> roles = resolveRecipientRoles();
        if (roles.isEmpty()) {
            return List.of();
        }
        return appUserRepository.findAllByIsActiveTrueAndStatusAndRoleInAndEmailIsNotNull(
                UserStatusEnum.ACTIVE,
                roles
        );
    }

    private List<Integer> resolveDaysBefore() {
        return Arrays.stream(daysBeforeProperty.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::parseNonNegativeInteger)
                .filter(value -> value != null)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();
    }

    private Set<UserRoleEnum> resolveRecipientRoles() {
        Set<UserRoleEnum> parsed = Arrays.stream(recipientRolesProperty.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(this::parseRole)
                .filter(role -> role != null)
                .collect(Collectors.toSet());
        return parsed.isEmpty() ? DEFAULT_ROLES : parsed;
    }

    private Integer parseNonNegativeInteger(String rawValue) {
        try {
            int value = Integer.parseInt(rawValue);
            return value >= 0 ? value : null;
        } catch (NumberFormatException ex) {
            log.warn("valor invalido em app.notifications.email.project-deadline.days-before: {}", rawValue);
            return null;
        }
    }

    private UserRoleEnum parseRole(String rawValue) {
        try {
            return UserRoleEnum.valueOf(rawValue.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warn("papel invalido em app.notifications.email.project-deadline.recipient-roles: {}", rawValue);
            return null;
        }
    }

    private ZoneId resolveZoneId() {
        try {
            return ZoneId.of(zoneIdProperty);
        } catch (DateTimeException ex) {
            log.warn("timezone invalido para notificacoes de prazo: {}. Usando America/Sao_Paulo", zoneIdProperty);
            return ZoneId.of("America/Sao_Paulo");
        }
    }

    private String normalizeEmail(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    public record ProjectDeadlineDispatchSummary(
            LocalDate referenceDate,
            int projectsEvaluated,
            int recipientsEvaluated,
            int emailsSent,
            int emailsSkipped,
            boolean notificationsEnabled,
            String message
    ) {
    }
}
