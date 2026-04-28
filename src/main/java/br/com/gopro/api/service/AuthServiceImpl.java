package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AuthForgotPasswordRequestDTO;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthResetPasswordRequestDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.dtos.MessageResponseDTO;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.model.PasswordResetToken;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.PasswordResetTokenRepository;
import br.com.gopro.api.service.audit.AuditEventRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_INVALID_CREDENTIALS = "Credenciais inválidas";
    private static final String FORGOT_PASSWORD_EMAIL_NOT_REGISTERED_MESSAGE =
            "Este e-mail não está cadastrado na plataforma.";
    private static final String FORGOT_PASSWORD_EMAIL_SENT_MESSAGE =
            "Você recebeu um e-mail com o link de redefinição de senha.";
    private static final String FORGOT_PASSWORD_EMAIL_FAILED_MESSAGE =
            "Infelizmente não foi possivel completar a operação. Tente novamente mais tarde.";
    private static final String INVALID_RESET_TOKEN_MESSAGE = "Token inválido ou expirado";
    private static final long MAX_AVATAR_SIZE_BYTES = 20L * 1024L * 1024L;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final RateLimitService rateLimitService;
    private final DocumentService documentService;
    private final PasswordPolicyService passwordPolicyService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    @Value("${app.auth.rate-limit.login.max-attempts:8}")
    private int loginRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.forgot-password.max-attempts:5}")
    private int forgotPasswordRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.reset-password.max-attempts:10}")
    private int resetPasswordRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.window-seconds:900}")
    private long rateLimitWindowSeconds;

    @Value("${app.auth.password-reset.expiration-minutes:30}")
    private long passwordResetExpirationMinutes;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    @Override
    public AuthLoginResponseDTO login(AuthLoginRequestDTO dto, HttpServletRequest request) {
        String normalizedLogin = normalizeLogin(dto.login());
        String rateLimitKey = buildLoginRateLimitKey(normalizedLogin, request);
        rateLimitService.ensureWithinLimit(rateLimitKey, loginRateLimitMaxAttempts, rateLimitWindowSeconds);

        AppUser user = appUserRepository.findByLogin(dto.login().trim())
                .orElseThrow(() -> {
                    rateLimitService.registerAttempt(rateLimitKey, rateLimitWindowSeconds);
                    auditLogService.log(AuditEventRequest.builder()
                                    .actorUserId(null)
                                    .tipoAuditoria(AuditScopeEnum.SYSTEM)
                                    .modulo("Sistema")
                                    .feature("Login")
                                    .entidadePrincipal("Usuario")
                                    .acao("LOGIN")
                                    .resultado(AuditResultEnum.FALHA)
                                    .detalhesTecnicos(Map.of(
                                            "auditAction", AuditActions.LOGIN_FAILED,
                                            "login", dto.login(),
                                            "reason", "NOT_FOUND"
                                    ))
                                    .build(),
                            request);
                    return new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
                });

        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getStatus() != UserStatusEnum.ACTIVE) {
            rateLimitService.registerAttempt(rateLimitKey, rateLimitWindowSeconds);
            auditLogService.log(AuditEventRequest.builder()
                            .actorUserId(user.getId())
                            .tipoAuditoria(AuditScopeEnum.SYSTEM)
                            .modulo("Sistema")
                            .feature("Login")
                            .entidadePrincipal("Usuario")
                            .entidadeId(String.valueOf(user.getId()))
                            .acao("LOGIN")
                            .resultado(AuditResultEnum.FALHA)
                            .detalhesTecnicos(Map.of(
                                    "auditAction", AuditActions.LOGIN_FAILED,
                                    "login", dto.login(),
                                    "reason", "INACTIVE_OR_DISABLED"
                            ))
                            .build(),
                    request);
            throw new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            rateLimitService.registerAttempt(rateLimitKey, rateLimitWindowSeconds);
            auditLogService.log(AuditEventRequest.builder()
                            .actorUserId(user.getId())
                            .tipoAuditoria(AuditScopeEnum.SYSTEM)
                            .modulo("Sistema")
                            .feature("Login")
                            .entidadePrincipal("Usuario")
                            .entidadeId(String.valueOf(user.getId()))
                            .acao("LOGIN")
                            .resultado(AuditResultEnum.FALHA)
                            .detalhesTecnicos(Map.of(
                                    "auditAction", AuditActions.LOGIN_FAILED,
                                    "login", dto.login(),
                                    "reason", "PASSWORD_MISMATCH"
                            ))
                            .build(),
                    request);
            throw new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
        }

        rateLimitService.reset(rateLimitKey);
        user.setLastLoginAt(LocalDateTime.now());
        appUserRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        AuthUserResponseDTO userDto = toAuthUserDTO(user);

        auditLogService.log(AuditEventRequest.builder()
                        .actorUserId(user.getId())
                        .tipoAuditoria(AuditScopeEnum.SYSTEM)
                        .modulo("Sistema")
                        .feature("Login")
                        .entidadePrincipal("Usuario")
                        .entidadeId(String.valueOf(user.getId()))
                        .acao("LOGIN")
                        .resultado(AuditResultEnum.SUCESSO)
                        .depois(Map.of("email", user.getEmail(), "role", user.getRole()))
                        .detalhesTecnicos(Map.of("auditAction", AuditActions.LOGIN_SUCCESS))
                        .build(),
                request);

        return new AuthLoginResponseDTO(
                accessToken,
                "Bearer",
                jwtService.getJwtExpirationSeconds(),
                userDto
        );
    }

    @Override
    @Transactional
    public MessageResponseDTO forgotPassword(AuthForgotPasswordRequestDTO dto, HttpServletRequest request) {
        String normalizedEmail = normalizeEmail(dto.email());
        String rateLimitKey = buildForgotPasswordRateLimitKey(normalizedEmail, request);
        rateLimitService.checkRateLimit(
                rateLimitKey,
                forgotPasswordRateLimitMaxAttempts,
                rateLimitWindowSeconds
        );

        Optional<AppUser> userOpt = appUserRepository.findByEmailIgnoreCase(normalizedEmail);
        if (userOpt.isEmpty()) {
            throw new BusinessException(FORGOT_PASSWORD_EMAIL_NOT_REGISTERED_MESSAGE);
        }

        AppUser user = userOpt.get();
        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BusinessException("Este usuário não está ativo na plataforma.");
        }

        passwordResetTokenRepository.invalidateExpiredActiveTokensByUserId(user.getId(), LocalDateTime.now(), user.getId());
        passwordResetTokenRepository.invalidateActiveTokensByUserId(user.getId(), user.getId());

        String rawToken = generateSecureToken();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setTokenHash(hashToken(rawToken));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(passwordResetExpirationMinutes));
        resetToken.setIsActive(true);
        resetToken.setCreatedBy(user.getId());
        resetToken.setUpdatedBy(user.getId());
        passwordResetTokenRepository.save(resetToken);

        String resetLink = buildPasswordResetLink(rawToken);
        EmailService.EmailDispatchResult emailResult = emailService.sendPasswordResetEmail(
                user.getEmail(),
                user.getFullName(),
                resetLink,
                resetToken.getExpiresAt()
        );
        if (!emailResult.success()) {
            log.warn(
                    "password_reset_email_failed userId={} status={} message={}",
                    user.getId(),
                    emailResult.statusCode(),
                    emailResult.message()
            );
        }

        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(user.getId())
                        .tipoAuditoria(AuditScopeEnum.SYSTEM)
                        .modulo("Sistema")
                        .feature("Recuperacao de senha")
                        .entidadePrincipal("Usuario")
                        .entidadeId(String.valueOf(user.getId()))
                        .acao("ATUALIZAR")
                        .resultado(emailResult.success() ? AuditResultEnum.SUCESSO : AuditResultEnum.FALHA)
                        .depois(Map.of("email", user.getEmail()))
                        .detalhesTecnicos(Map.of(
                                "auditAction", AuditActions.PASSWORD_RESET_REQUESTED,
                                "emailDispatchSuccess", emailResult.success()
                        ))
                        .build(),
                request
        );

        if (!emailResult.success()) {
            throw new BusinessException(FORGOT_PASSWORD_EMAIL_FAILED_MESSAGE);
        }

        return new MessageResponseDTO(FORGOT_PASSWORD_EMAIL_SENT_MESSAGE);
    }

    @Override
    @Transactional
    public MessageResponseDTO resetPassword(AuthResetPasswordRequestDTO dto, HttpServletRequest request) {
        String rateLimitKey = buildResetPasswordRateLimitKey(request);
        rateLimitService.checkRateLimit(
                rateLimitKey,
                resetPasswordRateLimitMaxAttempts,
                rateLimitWindowSeconds
        );

        passwordPolicyService.validateOrThrow(dto.newPassword());

        PasswordResetToken resetToken = findUsableResetToken(dto.token());
        AppUser user = resetToken.getUser();
        if (user == null || !Boolean.TRUE.equals(user.getIsActive()) || user.getStatus() != UserStatusEnum.ACTIVE) {
            throw new BusinessException(INVALID_RESET_TOKEN_MESSAGE);
        }

        LocalDateTime usedAt = LocalDateTime.now();
        user.setPasswordHash(passwordEncoder.encode(dto.newPassword()));
        user.setAuthTokensInvalidBefore(Instant.now());
        user.setUpdatedBy(user.getId());
        appUserRepository.save(user);

        resetToken.setUsedAt(usedAt);
        resetToken.setIsActive(false);
        resetToken.setUpdatedBy(user.getId());
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.invalidateActiveTokensByUserId(user.getId(), user.getId());

        auditLogService.log(
                AuditEventRequest.builder()
                        .actorUserId(user.getId())
                        .tipoAuditoria(AuditScopeEnum.SYSTEM)
                        .modulo("Sistema")
                        .feature("Recuperacao de senha")
                        .entidadePrincipal("Usuario")
                        .entidadeId(String.valueOf(user.getId()))
                        .acao("ATUALIZAR")
                        .resultado(AuditResultEnum.SUCESSO)
                        .depois(Map.of("email", user.getEmail()))
                        .detalhesTecnicos(Map.of("auditAction", AuditActions.PASSWORD_RESET_COMPLETED))
                        .build(),
                request
        );

        return new MessageResponseDTO("Senha redefinida com sucesso");
    }

    @Override
    public AuthUserResponseDTO me(AuthenticatedUserPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return toAuthUserDTO(user);
    }

    @Override
    public AuthUserResponseDTO updateMyAvatar(AuthenticatedUserPrincipal principal, MultipartFile file) {
        validateAvatarFile(file);

        AppUser user = appUserRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
        if (user.getRole() != null && user.getRole().isProtectedAccount()) {
            throw new AccessDeniedException("Acesso negado");
        }


        String avatarDocumentId = documentService
                .upload(file, DocumentOwnerTypeEnum.USER, user.getId(), "FOTO_PERFIL", principal.id())
                .id()
                .toString();

        user.setAvatarUrl(avatarDocumentId);
        user.setUpdatedBy(principal.id());

        AppUser savedUser = appUserRepository.save(user);
        return toAuthUserDTO(savedUser);
    }

    private AuthUserResponseDTO toAuthUserDTO(AppUser user) {
        return new AuthUserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getAvatarUrl(),
                user.getLastLoginAt()
        );
    }

    private PasswordResetToken findUsableResetToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new BusinessException(INVALID_RESET_TOKEN_MESSAGE);
        }

        String tokenHash = hashToken(rawToken.trim());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BusinessException(INVALID_RESET_TOKEN_MESSAGE));

        if (!Boolean.TRUE.equals(resetToken.getIsActive()) || resetToken.getUsedAt() != null) {
            throw new BusinessException(INVALID_RESET_TOKEN_MESSAGE);
        }
        if (resetToken.getExpiresAt() == null || !resetToken.getExpiresAt().isAfter(LocalDateTime.now())) {
            resetToken.setIsActive(false);
            passwordResetTokenRepository.save(resetToken);
            throw new BusinessException(INVALID_RESET_TOKEN_MESSAGE);
        }

        return resetToken;
    }

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo de foto obrigatorio");
        }

        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new BusinessException("A foto excede o tamanho maximo de 20MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException("A foto deve ser uma imagem válida");
        }
    }

    private String buildLoginRateLimitKey(String normalizedLogin, HttpServletRequest request) {
        return "login:" + extractClientIp(request) + ":" + normalizedLogin;
    }

    private String buildForgotPasswordRateLimitKey(String normalizedEmail, HttpServletRequest request) {
        return "forgot-password:" + extractClientIp(request) + ":" + normalizedEmail;
    }

    private String buildResetPasswordRateLimitKey(HttpServletRequest request) {
        return "reset-password:" + extractClientIp(request);
    }

    private String buildPasswordResetLink(String rawToken) {
        String base = appUrl == null ? "" : appUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + "/reset-password?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    private String normalizeLogin(String login) {
        if (login == null || login.isBlank()) {
            return "unknown";
        }
        return login.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessException("E-mail e obrigatorio");
        }
        return email.trim().toLowerCase(Locale.ROOT);
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
