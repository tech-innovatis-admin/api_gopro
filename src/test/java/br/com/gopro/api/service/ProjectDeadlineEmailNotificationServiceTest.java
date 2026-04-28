package br.com.gopro.api.service;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.Project;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.EmailNotificationDispatchRepository;
import br.com.gopro.api.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectDeadlineEmailNotificationServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private EmailNotificationDispatchRepository dispatchRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ProjectDeadlineEmailNotificationService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "enabled", true);
        ReflectionTestUtils.setField(service, "daysBeforeProperty", "30,7,1");
        ReflectionTestUtils.setField(service, "recipientRolesProperty", "SUPERADMIN,ADMIN,OWNER");
        ReflectionTestUtils.setField(service, "zoneIdProperty", "America/Sao_Paulo");
        ReflectionTestUtils.setField(service, "batchSize", 50);
    }

    @Test
    void notifyProjectDeadlines_shouldSendForConfiguredMilestone() {
        LocalDate today = LocalDate.of(2026, 4, 23);
        Project project = project(11L, "CT-2026-001", "Contrato teste", today.plusDays(7));
        AppUser user = user("gestor@empresa.com", "Gestor");

        when(appUserRepository.findAllByIsActiveTrueAndStatusAndRoleInAndEmailIsNotNull(
                eq(UserStatusEnum.ACTIVE),
                eq(Set.of(UserRoleEnum.SUPERADMIN, UserRoleEnum.ADMIN, UserRoleEnum.OWNER))
        )).thenReturn(List.of(user));
        when(projectRepository.findExpiringProjectsBetween(eq(today), eq(today.plusDays(30)), any()))
                .thenReturn(List.of(project));
        when(dispatchRepository.existsByNotificationTypeAndEntityTypeAndEntityIdAndRecipientEmailIgnoreCaseAndReferenceDate(
                "PROJECT_DEADLINE",
                "PROJECT",
                11L,
                "gestor@empresa.com",
                project.getEndDate().minusDays(7)
        )).thenReturn(false);
        when(emailService.sendProjectDeadlineNotification("gestor@empresa.com", "Gestor", project, 7))
                .thenReturn(new EmailService.EmailDispatchResult(true, 202, null, null, "ok"));

        service.notifyProjectDeadlines(today);

        verify(emailService).sendProjectDeadlineNotification("gestor@empresa.com", "Gestor", project, 7);
        verify(dispatchRepository).saveAndFlush(any());
    }

    @Test
    void notifyProjectDeadlines_shouldSkipAlreadySentDispatch() {
        LocalDate today = LocalDate.of(2026, 4, 23);
        Project project = project(11L, "CT-2026-001", "Contrato teste", today.plusDays(1));
        AppUser user = user("gestor@empresa.com", "Gestor");

        when(appUserRepository.findAllByIsActiveTrueAndStatusAndRoleInAndEmailIsNotNull(
                eq(UserStatusEnum.ACTIVE),
                eq(Set.of(UserRoleEnum.SUPERADMIN, UserRoleEnum.ADMIN, UserRoleEnum.OWNER))
        )).thenReturn(List.of(user));
        when(projectRepository.findExpiringProjectsBetween(eq(today), eq(today.plusDays(30)), any()))
                .thenReturn(List.of(project));
        when(dispatchRepository.existsByNotificationTypeAndEntityTypeAndEntityIdAndRecipientEmailIgnoreCaseAndReferenceDate(
                "PROJECT_DEADLINE",
                "PROJECT",
                11L,
                "gestor@empresa.com",
                project.getEndDate().minusDays(1)
        )).thenReturn(true);

        service.notifyProjectDeadlines(today);

        verify(emailService, never()).sendProjectDeadlineNotification(any(), any(), any(), anyLong());
        verify(dispatchRepository, never()).saveAndFlush(any());
    }

    @Test
    void notifyProjectDeadlines_shouldIgnoreProjectsOutsideMilestone() {
        LocalDate today = LocalDate.of(2026, 4, 23);
        Project project = project(11L, "CT-2026-001", "Contrato teste", today.plusDays(5));
        AppUser user = user("gestor@empresa.com", "Gestor");

        when(appUserRepository.findAllByIsActiveTrueAndStatusAndRoleInAndEmailIsNotNull(
                eq(UserStatusEnum.ACTIVE),
                eq(Set.of(UserRoleEnum.SUPERADMIN, UserRoleEnum.ADMIN, UserRoleEnum.OWNER))
        )).thenReturn(List.of(user));
        when(projectRepository.findExpiringProjectsBetween(eq(today), eq(today.plusDays(30)), any()))
                .thenReturn(List.of(project));

        service.notifyProjectDeadlines(today);

        verify(emailService, never()).sendProjectDeadlineNotification(any(), any(), any(), anyLong());
        verify(dispatchRepository, never()).saveAndFlush(any());
    }

    @Test
    void notifyProjectDeadlines_shouldRecoverMissedMilestoneOnNextDay() {
        LocalDate today = LocalDate.of(2026, 4, 24);
        Project project = project(11L, "CT-2026-001", "Contrato teste", today.plusDays(29));
        project.setCreatedAt(LocalDateTime.of(2026, 4, 23, 9, 5));
        AppUser user = user("gestor@empresa.com", "Gestor");
        LocalDate milestoneDate = today.minusDays(1);

        when(appUserRepository.findAllByIsActiveTrueAndStatusAndRoleInAndEmailIsNotNull(
                eq(UserStatusEnum.ACTIVE),
                eq(Set.of(UserRoleEnum.SUPERADMIN, UserRoleEnum.ADMIN, UserRoleEnum.OWNER))
        )).thenReturn(List.of(user));
        when(projectRepository.findExpiringProjectsBetween(eq(today), eq(today.plusDays(30)), any()))
                .thenReturn(List.of(project));
        when(dispatchRepository.existsByNotificationTypeAndEntityTypeAndEntityIdAndRecipientEmailIgnoreCaseAndReferenceDate(
                "PROJECT_DEADLINE",
                "PROJECT",
                11L,
                "gestor@empresa.com",
                milestoneDate
        )).thenReturn(false);
        when(emailService.sendProjectDeadlineNotification("gestor@empresa.com", "Gestor", project, 29))
                .thenReturn(new EmailService.EmailDispatchResult(true, 202, null, null, "ok"));

        service.notifyProjectDeadlines(today);

        verify(emailService).sendProjectDeadlineNotification("gestor@empresa.com", "Gestor", project, 29);
        verify(dispatchRepository).saveAndFlush(argThat(dispatch ->
                "PROJECT_DEADLINE".equals(dispatch.getNotificationType())
                        && "PROJECT".equals(dispatch.getEntityType())
                        && Long.valueOf(11L).equals(dispatch.getEntityId())
                        && "gestor@empresa.com".equals(dispatch.getRecipientEmail())
                        && milestoneDate.equals(dispatch.getReferenceDate())
        ));
    }

    private Project project(Long id, String code, String name, LocalDate endDate) {
        Project project = new Project();
        project.setId(id);
        project.setCode(code);
        project.setName(name);
        project.setEndDate(endDate);
        project.setIsActive(true);
        return project;
    }

    private AppUser user(String email, String fullName) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(UserRoleEnum.ADMIN);
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setIsActive(true);
        return user;
    }
}
