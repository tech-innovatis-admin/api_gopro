package br.com.gopro.api.config;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.AuditActions;
import br.com.gopro.api.service.AuditLogService;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.service.audit.AuditEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminBootstrapInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Value("${app.auth.bootstrap.superadmin.enabled:false}")
    private boolean bootstrapEnabled;

    @Value("${app.auth.bootstrap.superadmin.email:}")
    private String bootstrapEmail;

    @Value("${app.auth.bootstrap.superadmin.username:}")
    private String bootstrapUsername;

    @Value("${app.auth.bootstrap.superadmin.password:}")
    private String bootstrapPassword;

    @Value("${app.auth.bootstrap.superadmin.full-name:Tech Innovatis}")
    private String bootstrapFullName;

    @Override
    public void run(ApplicationArguments args) {
        if (!bootstrapEnabled) {
            return;
        }

        if (isBlank(bootstrapEmail) || isBlank(bootstrapPassword)) {
            log.warn("superadmin bootstrap habilitado, mas email/senha nao foram informados");
            return;
        }

        String normalizedEmail = bootstrapEmail.trim().toLowerCase();
        String normalizedUsername = isBlank(bootstrapUsername) ? "tech" : bootstrapUsername.trim().toLowerCase();
        Optional<AppUser> existing = resolveBootstrapUser(normalizedEmail, normalizedUsername);

        boolean hasActivePrivilegedUser = appUserRepository.existsByRoleInAndStatusAndIsActive(
                List.of(UserRoleEnum.OWNER, UserRoleEnum.SUPERADMIN),
                UserStatusEnum.ACTIVE,
                true
        );
        if (hasActivePrivilegedUser) {
            if (existing.isPresent() && isActivePrivilegedUser(existing.get())) {
                log.info("superadmin bootstrap ignorado: usuario bootstrap ja existe e esta ativo");
            } else {
                log.info("superadmin bootstrap ignorado: ja existe OWNER ou SUPERADMIN ativo");
            }
            return;
        }

        AppUser superAdmin = existing.orElseGet(AppUser::new);
        boolean created = superAdmin.getId() == null;

        superAdmin.setEmail(normalizedEmail);
        superAdmin.setUsername(normalizedUsername);
        superAdmin.setFullName(isBlank(bootstrapFullName) ? "Tech Innovatis" : bootstrapFullName.trim());
        superAdmin.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        superAdmin.setRole(UserRoleEnum.SUPERADMIN);
        superAdmin.setStatus(UserStatusEnum.ACTIVE);
        superAdmin.setIsActive(true);
        if (created) {
            superAdmin.setCreatedBy(null);
        } else {
            superAdmin.setUpdatedBy(superAdmin.getId());
        }

        AppUser saved = appUserRepository.save(superAdmin);
        log.warn("superadmin bootstrap executado para email={}", saved.getEmail());

        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(saved.getId())
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuarios")
                        .feature("Bootstrap de superadmin")
                        .entidadePrincipal("Usuario")
                        .entidadeId(String.valueOf(saved.getId()))
                        .acao("CRIAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .depois(Map.of("email", saved.getEmail(), "created", created))
                        .detalhesTecnicos(Map.of("auditAction", AuditActions.SUPERADMIN_BOOTSTRAPPED))
                        .build(),
                null
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Optional<AppUser> resolveBootstrapUser(String email, String username) {
        Optional<AppUser> existing = appUserRepository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) {
            return existing;
        }

        if (isBlank(username)) {
            return Optional.empty();
        }

        return appUserRepository.findByUsernameIgnoreCase(username);
    }

    private boolean isActivePrivilegedUser(AppUser user) {
        return user.getRole() != null
                && user.getRole().hasSuperadminPrivileges()
                && user.getStatus() == UserStatusEnum.ACTIVE
                && Boolean.TRUE.equals(user.getIsActive());
    }
}
