package br.com.gopro.api.config;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.AuditActions;
import br.com.gopro.api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

        boolean hasActiveSuperAdmin = appUserRepository.existsByRoleAndStatusAndIsActive(
                UserRoleEnum.SUPERADMIN,
                UserStatusEnum.ACTIVE,
                true
        );
        if (hasActiveSuperAdmin) {
            log.info("superadmin bootstrap ignorado: ja existe SUPERADMIN ativo");
            return;
        }

        String normalizedEmail = bootstrapEmail.trim().toLowerCase();
        Optional<AppUser> existing = appUserRepository.findByEmailIgnoreCase(normalizedEmail);
        AppUser superAdmin = existing.orElseGet(AppUser::new);
        boolean created = superAdmin.getId() == null;

        superAdmin.setEmail(normalizedEmail);
        superAdmin.setUsername(isBlank(bootstrapUsername) ? "tech" : bootstrapUsername.trim().toLowerCase());
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
                saved.getId(),
                AuditActions.SUPERADMIN_BOOTSTRAPPED,
                "users",
                String.valueOf(saved.getId()),
                null,
                Map.of("email", saved.getEmail(), "created", created),
                null
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
