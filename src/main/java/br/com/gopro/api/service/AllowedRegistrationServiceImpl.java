package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.*;
import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.AllowedRegistration;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AllowedRegistrationRepository;
import br.com.gopro.api.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AllowedRegistrationServiceImpl implements AllowedRegistrationService {

    private static final String INVALID_INVITE_MESSAGE = "Convite invalido ou expirado";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AllowedRegistrationRepository allowedRegistrationRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final PasswordPolicyService passwordPolicyService;
    private final RateLimitService rateLimitService;

    @Value("${app.auth.invite.base-url:http://localhost:3000/register}")
    private String inviteBaseUrl;

    @Value("${app.auth.invite.default-expiration-hours:72}")
    private long defaultInviteExpirationHours;

    @Value("${app.auth.rate-limit.register.max-attempts:12}")
    private int registerRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.window-seconds:900}")
    private long rateLimitWindowSeconds;

    @Override
    public AllowedRegistrationResponseDTO createInvite(
            AllowedRegistrationCreateRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    ) {
        expirePendingInvites();

        AppUser inviter = appUserRepository.findById(actor.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));

        String normalizedEmail = normalizeEmail(dto.email());
        LocalDateTime expiresAt = resolveExpiration(dto.expiresAt());

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        Optional<AllowedRegistration> existingOpt = allowedRegistrationRepository.findByEmailIgnoreCase(normalizedEmail);
        boolean isCreate = existingOpt.isEmpty();
        AllowedRegistration invite = existingOpt.orElseGet(AllowedRegistration::new);
        Map<String, Object> before = invite.getId() != null ? snapshot(invite) : null;

        if (invite.getId() == null) {
            invite.setCreatedBy(actor.id());
        }
        invite.setEmail(normalizedEmail);
        invite.setRole(dto.role());
        invite.setInviteTokenHash(tokenHash);
        invite.setInvitedByUser(inviter);
        invite.setInvitedAt(LocalDateTime.now());
        invite.setExpiresAt(expiresAt);
        invite.setUsedAt(null);
        invite.setStatus(AllowedRegistrationStatusEnum.PENDING);
        invite.setIsActive(true);
        invite.setUpdatedBy(actor.id());

        AllowedRegistration saved = allowedRegistrationRepository.save(invite);
        AllowedRegistrationResponseDTO response = toDTO(saved, buildInviteLink(rawToken));

        auditLogService.log(
                actor.id(),
                isCreate ? AuditActions.INVITE_CREATED : AuditActions.INVITE_REISSUED,
                "allowed_registrations",
                String.valueOf(saved.getId()),
                before,
                snapshot(saved),
                request
        );

        return response;
    }

    @Override
    public PageResponseDTO<AllowedRegistrationResponseDTO> listInvites(
            AllowedRegistrationStatusEnum status,
            int page,
            int size
    ) {
        expirePendingInvites();

        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Specification<AllowedRegistration> specification = Specification.where(null);
        if (status != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<AllowedRegistration> invitePage = allowedRegistrationRepository.findAll(specification, pageable);
        List<AllowedRegistrationResponseDTO> content = invitePage.getContent().stream()
                .map(invite -> toDTO(invite, null))
                .toList();

        return new PageResponseDTO<>(
                content,
                invitePage.getNumber(),
                invitePage.getSize(),
                invitePage.getTotalElements(),
                invitePage.getTotalPages(),
                invitePage.isFirst(),
                invitePage.isLast()
        );
    }

    @Override
    public AllowedRegistrationResponseDTO cancelInvite(
            Long id,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    ) {
        AllowedRegistration invite = allowedRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convite nao encontrado"));

        if (invite.getStatus() == AllowedRegistrationStatusEnum.USED) {
            throw new BusinessException("Nao e possivel cancelar convite ja utilizado");
        }
        if (invite.getStatus() == AllowedRegistrationStatusEnum.CANCELLED) {
            throw new BusinessException("Convite ja esta cancelado");
        }

        Map<String, Object> before = snapshot(invite);
        invite.setStatus(AllowedRegistrationStatusEnum.CANCELLED);
        invite.setIsActive(false);
        invite.setUpdatedBy(actor.id());

        AllowedRegistration saved = allowedRegistrationRepository.save(invite);
        auditLogService.log(
                actor.id(),
                AuditActions.INVITE_CANCELLED,
                "allowed_registrations",
                String.valueOf(saved.getId()),
                before,
                snapshot(saved),
                request
        );

        return toDTO(saved, null);
    }

    @Override
    public AllowedRegistrationResponseDTO reissueInvite(
            Long id,
            AllowedRegistrationReissueRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    ) {
        AllowedRegistration invite = allowedRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convite nao encontrado"));

        String normalizedEmail = normalizeEmail(invite.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("Ja existe usuario ativo para este e-mail");
        }

        AppUser inviter = appUserRepository.findById(actor.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado nao encontrado"));

        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);
        Map<String, Object> before = snapshot(invite);

        invite.setInviteTokenHash(tokenHash);
        invite.setInvitedByUser(inviter);
        invite.setInvitedAt(LocalDateTime.now());
        invite.setExpiresAt(resolveExpiration(dto != null ? dto.expiresAt() : null));
        invite.setUsedAt(null);
        invite.setStatus(AllowedRegistrationStatusEnum.PENDING);
        invite.setIsActive(true);
        invite.setUpdatedBy(actor.id());

        AllowedRegistration saved = allowedRegistrationRepository.save(invite);
        auditLogService.log(
                actor.id(),
                AuditActions.INVITE_REISSUED,
                "allowed_registrations",
                String.valueOf(saved.getId()),
                before,
                snapshot(saved),
                request
        );

        return toDTO(saved, buildInviteLink(rawToken));
    }

    @Override
    public AllowedRegistrationValidationResponseDTO validateInviteToken(String token, HttpServletRequest request) {
        String key = "register:validate:" + extractClientIp(request);
        rateLimitService.checkRateLimit(key, registerRateLimitMaxAttempts, rateLimitWindowSeconds);

        AllowedRegistration invite = findPendingInviteByToken(token);
        ensureInviteUsableOrThrow(invite);

        auditLogService.log(
                invite.getInvitedByUser() != null ? invite.getInvitedByUser().getId() : null,
                AuditActions.INVITE_VALIDATED,
                "allowed_registrations",
                String.valueOf(invite.getId()),
                null,
                Map.of("email", invite.getEmail()),
                request
        );

        return new AllowedRegistrationValidationResponseDTO(
                invite.getEmail(),
                invite.getRole(),
                invite.getExpiresAt()
        );
    }

    @Override
    public RegisterCompleteResponseDTO completeRegistration(RegisterCompleteRequestDTO dto, HttpServletRequest request) {
        String key = "register:complete:" + extractClientIp(request);
        rateLimitService.checkRateLimit(key, registerRateLimitMaxAttempts, rateLimitWindowSeconds);

        AllowedRegistration invite = findPendingInviteByToken(dto.token());
        ensureInviteUsableOrThrow(invite);

        passwordPolicyService.validateOrThrow(dto.password());

        String normalizedEmail = normalizeEmail(invite.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("Nao foi possivel concluir cadastro com este convite");
        }

        String username = resolveUsername(dto.username(), normalizedEmail);

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setUsername(username);
        user.setFullName(dto.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(dto.password()));
        user.setRole(invite.getRole());
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setIsActive(true);
        user.setLastLoginAt(LocalDateTime.now());
        user.setCreatedBy(invite.getInvitedByUser() != null ? invite.getInvitedByUser().getId() : null);

        AppUser savedUser = appUserRepository.save(user);

        Map<String, Object> inviteBefore = snapshot(invite);
        invite.setStatus(AllowedRegistrationStatusEnum.USED);
        invite.setUsedAt(LocalDateTime.now());
        invite.setUpdatedBy(savedUser.getId());
        allowedRegistrationRepository.save(invite);

        String accessToken = jwtService.generateAccessToken(savedUser);
        AuthUserResponseDTO authUser = new AuthUserResponseDTO(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole(),
                savedUser.getStatus()
        );
        AuthLoginResponseDTO auth = new AuthLoginResponseDTO(
                accessToken,
                "Bearer",
                jwtService.getJwtExpirationSeconds(),
                authUser
        );

        auditLogService.log(
                savedUser.getId(),
                AuditActions.REGISTER_COMPLETED,
                "users",
                String.valueOf(savedUser.getId()),
                null,
                Map.of(
                        "email", savedUser.getEmail(),
                        "role", savedUser.getRole(),
                        "status", savedUser.getStatus()
                ),
                request
        );
        auditLogService.log(
                savedUser.getId(),
                AuditActions.REGISTER_COMPLETED,
                "allowed_registrations",
                String.valueOf(invite.getId()),
                inviteBefore,
                snapshot(invite),
                request
        );

        return new RegisterCompleteResponseDTO("Cadastro concluido com sucesso", auth);
    }

    private AllowedRegistration findPendingInviteByToken(String token) {
        if (token == null || token.isBlank()) {
            throw new BusinessException(INVALID_INVITE_MESSAGE);
        }
        String tokenHash = hashToken(token.trim());
        return allowedRegistrationRepository.findByInviteTokenHashAndStatus(
                        tokenHash,
                        AllowedRegistrationStatusEnum.PENDING
                )
                .orElseThrow(() -> new BusinessException(INVALID_INVITE_MESSAGE));
    }

    private void ensureInviteUsableOrThrow(AllowedRegistration invite) {
        if (invite.getStatus() != AllowedRegistrationStatusEnum.PENDING) {
            throw new BusinessException(INVALID_INVITE_MESSAGE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (invite.getExpiresAt() == null || !invite.getExpiresAt().isAfter(now)) {
            invite.setStatus(AllowedRegistrationStatusEnum.EXPIRED);
            invite.setIsActive(false);
            allowedRegistrationRepository.save(invite);
            throw new BusinessException(INVALID_INVITE_MESSAGE);
        }
    }

    private LocalDateTime resolveExpiration(LocalDateTime requestedExpiration) {
        LocalDateTime fallback = LocalDateTime.now().plusHours(defaultInviteExpirationHours);
        if (requestedExpiration == null) {
            return fallback;
        }
        if (!requestedExpiration.isAfter(LocalDateTime.now())) {
            throw new BusinessException("Data de expiracao deve ser futura");
        }
        return requestedExpiration;
    }

    private String buildInviteLink(String rawToken) {
        String separator = inviteBaseUrl.contains("?") ? "&" : "?";
        return inviteBaseUrl + separator + "token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 nao disponivel", exception);
        }
    }

    private AllowedRegistrationResponseDTO toDTO(AllowedRegistration invite, String inviteLink) {
        Long invitedBy = invite.getInvitedByUser() != null ? invite.getInvitedByUser().getId() : null;
        return new AllowedRegistrationResponseDTO(
                invite.getId(),
                invite.getEmail(),
                invite.getRole(),
                invite.getStatus(),
                invitedBy,
                invite.getInvitedAt(),
                invite.getExpiresAt(),
                invite.getUsedAt(),
                inviteLink
        );
    }

    private Map<String, Object> snapshot(AllowedRegistration invite) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", invite.getId());
        map.put("email", invite.getEmail());
        map.put("role", invite.getRole());
        map.put("status", invite.getStatus());
        map.put("expiresAt", invite.getExpiresAt());
        map.put("usedAt", invite.getUsedAt());
        map.put("invitedByUserId", invite.getInvitedByUser() != null ? invite.getInvitedByUser().getId() : null);
        return map;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("E-mail e obrigatorio");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveUsername(String requestedUsername, String email) {
        String base = requestedUsername;
        if (base == null || base.isBlank()) {
            int atIndex = email.indexOf('@');
            base = atIndex > 0 ? email.substring(0, atIndex) : "user";
        }

        String normalizedBase = base.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (normalizedBase.isBlank()) {
            normalizedBase = "user";
        }
        if (normalizedBase.length() > 100) {
            normalizedBase = normalizedBase.substring(0, 100);
        }

        String candidate = normalizedBase;
        int suffix = 1;
        while (appUserRepository.existsByUsernameIgnoreCase(candidate)) {
            String suffixText = "_" + suffix;
            int maxBaseLength = 100 - suffixText.length();
            String safeBase = normalizedBase.length() > maxBaseLength
                    ? normalizedBase.substring(0, maxBaseLength)
                    : normalizedBase;
            candidate = safeBase + suffixText;
            suffix++;
            if (suffix > 9999) {
                throw new BusinessException("Nao foi possivel gerar username unico");
            }
        }
        return candidate;
    }

    private void expirePendingInvites() {
        LocalDateTime now = LocalDateTime.now();
        List<AllowedRegistration> pendingExpired = allowedRegistrationRepository.findAll((root, query, cb) ->
                cb.and(
                        cb.equal(root.get("status"), AllowedRegistrationStatusEnum.PENDING),
                        cb.lessThanOrEqualTo(root.get("expiresAt"), now)
                ));

        if (pendingExpired.isEmpty()) {
            return;
        }

        pendingExpired.forEach(invite -> {
            invite.setStatus(AllowedRegistrationStatusEnum.EXPIRED);
            invite.setIsActive(false);
        });
        allowedRegistrationRepository.saveAll(pendingExpired);
    }

    private String extractClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
