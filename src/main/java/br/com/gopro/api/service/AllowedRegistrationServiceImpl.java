package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AllowedRegistrationCreateRequestDTO;
import br.com.gopro.api.dtos.AllowedRegistrationReissueRequestDTO;
import br.com.gopro.api.dtos.AllowedRegistrationResponseDTO;
import br.com.gopro.api.dtos.AllowedRegistrationValidationResponseDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.dtos.RegisterCompleteRequestDTO;
import br.com.gopro.api.dtos.RegisterCompleteResponseDTO;
import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.AllowedRegistration;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AllowedRegistrationRepository;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.audit.AuditEventRequest;
import br.com.gopro.api.service.audit.AuditFieldChange;
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
import org.springframework.transaction.annotation.Transactional;

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

    private static final String INVALID_INVITE_MESSAGE = "Convite inválido ou expirado";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AllowedRegistrationRepository allowedRegistrationRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final PasswordPolicyService passwordPolicyService;
    private final RateLimitService rateLimitService;
    private final EmailService emailService;

    @Value("${app.auth.invite.base-url:http://localhost:3000/register}")
    private String inviteBaseUrl;

    @Value("${app.auth.invite.default-expiration-hours:72}")
    private long defaultInviteExpirationHours;

    @Value("${app.auth.rate-limit.register.max-attempts:12}")
    private int registerRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.window-seconds:900}")
    private long rateLimitWindowSeconds;

    @Override
    @Transactional
    public AllowedRegistrationResponseDTO createInvite(
            AllowedRegistrationCreateRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    ) {
        expirePendingInvites();

        AppUser inviter = appUserRepository.findById(actor.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

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
        String inviteLink = buildInviteLink(rawToken);

        EmailService.EmailDispatchResult emailResult = emailService.sendInviteEmail(
                saved.getEmail(),
                saved.getRole(),
                inviteLink,
                saved.getExpiresAt()
        );
        if (!emailResult.success()) {
            throw new BusinessException("Nao foi possivel enviar o email do convite: " + emailResult.message());
        }

        AllowedRegistrationResponseDTO response = toDTO(saved, inviteLink);

        Map<String, Object> after = snapshot(saved);
        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(actor.id())
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuários")
                        .feature("Convites de cadastro")
                        .entidadePrincipal("Convite de cadastro")
                        .entidadeId(String.valueOf(saved.getId()))
                        .acao(isCreate ? "CRIAR" : "ATUALIZAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .antes(before)
                        .depois(after)
                        .alteracoes(buildChanges(before, after))
                        .detalhesTecnicos(Map.of(
                                "auditAction", isCreate ? AuditActions.INVITE_CREATED : AuditActions.INVITE_REISSUED,
                                "inviteAction", isCreate ? AuditActions.INVITE_CREATED : AuditActions.INVITE_REISSUED
                        ))
                        .build(),
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
            throw new BusinessException("Página deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da página deve estar entre 1 e 100");
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
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        if (invite.getStatus() == AllowedRegistrationStatusEnum.USED) {
            throw new BusinessException("Não é possível cancelar convite já utilizado");
        }
        if (invite.getStatus() == AllowedRegistrationStatusEnum.CANCELLED) {
            throw new BusinessException("Convite já está cancelado");
        }

        Map<String, Object> before = snapshot(invite);
        invite.setStatus(AllowedRegistrationStatusEnum.CANCELLED);
        invite.setIsActive(false);
        invite.setUpdatedBy(actor.id());

        AllowedRegistration saved = allowedRegistrationRepository.save(invite);
        Map<String, Object> after = snapshot(saved);
        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(actor.id())
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuários")
                        .feature("Convites de cadastro")
                        .entidadePrincipal("Convite de cadastro")
                        .entidadeId(String.valueOf(saved.getId()))
                        .acao("ATUALIZAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .antes(before)
                        .depois(after)
                        .alteracoes(buildChanges(before, after))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.INVITE_CANCELLED,
                                "inviteAction", AuditActions.INVITE_CANCELLED
                        ))
                        .build(),
                request
        );

        return toDTO(saved, null);
    }

    @Override
    @Transactional
    public AllowedRegistrationResponseDTO reissueInvite(
            Long id,
            AllowedRegistrationReissueRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    ) {
        AllowedRegistration invite = allowedRegistrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        String normalizedEmail = normalizeEmail(invite.getEmail());
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new BusinessException("Já existe usuário ativo para este e-mail");
        }

        AppUser inviter = appUserRepository.findById(actor.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));

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
        String inviteLink = buildInviteLink(rawToken);

        EmailService.EmailDispatchResult emailResult = emailService.sendInviteEmail(
                saved.getEmail(),
                saved.getRole(),
                inviteLink,
                saved.getExpiresAt()
        );
        if (!emailResult.success()) {
            throw new BusinessException("Nao foi possivel enviar o email do convite: " + emailResult.message());
        }

        Map<String, Object> after = snapshot(saved);
        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(actor.id())
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuarios")
                        .feature("Convites de cadastro")
                        .entidadePrincipal("Convite de cadastro")
                        .entidadeId(String.valueOf(saved.getId()))
                        .acao("ATUALIZAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .antes(before)
                        .depois(after)
                        .alteracoes(buildChanges(before, after))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.INVITE_REISSUED,
                                "inviteAction", AuditActions.INVITE_REISSUED
                        ))
                        .build(),
                request
        );

        return toDTO(saved, inviteLink);
    }

    @Override
    public AllowedRegistrationValidationResponseDTO validateInviteToken(String token, HttpServletRequest request) {
        String key = "register:validate:" + extractClientIp(request);
        rateLimitService.checkRateLimit(key, registerRateLimitMaxAttempts, rateLimitWindowSeconds);

        AllowedRegistration invite = findPendingInviteByToken(token);
        ensureInviteUsableOrThrow(invite);

        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(invite.getInvitedByUser() != null ? invite.getInvitedByUser().getId() : null)
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuarios")
                        .feature("Validacao de convite")
                        .entidadePrincipal("Convite de cadastro")
                        .entidadeId(String.valueOf(invite.getId()))
                        .acao("ATUALIZAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .depois(Map.of("email", invite.getEmail()))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.INVITE_VALIDATED,
                                "inviteAction", AuditActions.INVITE_VALIDATED
                        ))
                        .build(),
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

        String username = resolveUsername(dto.username());

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
                savedUser.getStatus(),
                savedUser.getAvatarUrl(),
                savedUser.getLastLoginAt()
        );
        AuthLoginResponseDTO auth = new AuthLoginResponseDTO(
                accessToken,
                "Bearer",
                jwtService.getJwtExpirationSeconds(),
                authUser
        );

        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(savedUser.getId())
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuarios")
                        .feature("Cadastro por convite")
                        .entidadePrincipal("Usuario")
                        .entidadeId(String.valueOf(savedUser.getId()))
                        .acao("CRIAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .depois(Map.of(
                                "email", savedUser.getEmail(),
                                "fullName", savedUser.getFullName(),
                                "role", savedUser.getRole(),
                                "status", savedUser.getStatus()
                        ))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.REGISTER_COMPLETED,
                                "inviteAction", AuditActions.REGISTER_COMPLETED
                        ))
                        .build(),
                request
        );
        Map<String, Object> inviteAfter = snapshot(invite);
        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(savedUser.getId())
                        .tipoAuditoria(AuditScopeEnum.USERS)
                        .modulo("Usuarios")
                        .feature("Cadastro por convite")
                        .entidadePrincipal("Convite de cadastro")
                        .entidadeId(String.valueOf(invite.getId()))
                        .acao("ATUALIZAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .antes(inviteBefore)
                        .depois(inviteAfter)
                        .alteracoes(buildChanges(inviteBefore, inviteAfter))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.REGISTER_COMPLETED,
                                "inviteAction", AuditActions.REGISTER_COMPLETED
                        ))
                        .build(),
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

    private List<AuditFieldChange> buildChanges(Map<String, Object> before, Map<String, Object> after) {
        if (before == null || after == null) {
            return List.of();
        }
        List<AuditFieldChange> changes = new ArrayList<>();
        for (Map.Entry<String, Object> entry : before.entrySet()) {
            Object oldValue = entry.getValue();
            Object newValue = after.get(entry.getKey());
            if ((oldValue == null && newValue == null) || (oldValue != null && oldValue.equals(newValue))) {
                continue;
            }
            changes.add(new AuditFieldChange(entry.getKey(), oldValue, newValue, "EDITADO"));
        }
        return changes;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("E-mail e obrigatorio");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String resolveUsername(String requestedUsername) {
        if (requestedUsername == null || requestedUsername.isBlank()) {
            throw new BusinessException("Username e obrigatorio");
        }

        String normalizedBase = requestedUsername.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (normalizedBase.isBlank()) {
            throw new BusinessException("Username invalido");
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
