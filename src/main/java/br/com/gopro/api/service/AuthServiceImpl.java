package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.DocumentOwnerTypeEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.audit.AuditEventRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_INVALID_CREDENTIALS = "Credenciais invalidas";
    private static final long MAX_AVATAR_SIZE_BYTES = 20L * 1024L * 1024L;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final RateLimitService rateLimitService;
    private final DocumentService documentService;

    @Value("${app.auth.rate-limit.login.max-attempts:8}")
    private int loginRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.window-seconds:900}")
    private long rateLimitWindowSeconds;

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
                            .entidadePrincipal("Usuário")
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
    public AuthUserResponseDTO me(AuthenticatedUserPrincipal principal) {
        AppUser user = appUserRepository.findById(principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
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

    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo de foto obrigatorio");
        }

        if (file.getSize() > MAX_AVATAR_SIZE_BYTES) {
            throw new BusinessException("A foto excede o tamanho maximo de 20MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException("A foto deve ser uma imagem valida");
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

    private String buildLoginRateLimitKey(String normalizedLogin, HttpServletRequest request) {
        return "login:" + extractClientIp(request) + ":" + normalizedLogin;
    }

    private String normalizeLogin(String login) {
        if (login == null || login.isBlank()) {
            return "unknown";
        }
        return login.trim().toLowerCase(Locale.ROOT);
    }
}
