package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.enums.AuditResultEnum;
import br.com.gopro.api.enums.AuditScopeEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.audit.AuditEventRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_INVALID_CREDENTIALS = "Credenciais invalidas";

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final RateLimitService rateLimitService;

    @Value("${app.auth.rate-limit.login.max-attempts:8}")
    private int loginRateLimitMaxAttempts;

    @Value("${app.auth.rate-limit.window-seconds:900}")
    private long rateLimitWindowSeconds;

    @Override
    public AuthLoginResponseDTO login(AuthLoginRequestDTO dto, HttpServletRequest request) {
        String rateLimitKey = "login:" + extractClientIp(request);
        rateLimitService.checkRateLimit(rateLimitKey, loginRateLimitMaxAttempts, rateLimitWindowSeconds);

        AppUser user = appUserRepository.findByLogin(dto.login().trim())
                .orElseThrow(() -> {
                    auditLogService.log(AuditEventRequest.builder()
                                    .actorUserId(null)
                                    .tipoAuditoria(AuditScopeEnum.SYSTEM)
                                    .modulo("Sistema")
                                    .feature("Login")
                                    .entidadePrincipal("Usuario")
                                    .acao("LOGIN")
                                    .resultado(AuditResultEnum.FALHA)
                                    .resumo("Falha de login para '" + dto.login() + "'")
                                    .descricao("Tentativa de acesso negada por credenciais invalidas.")
                                    .detalhesTecnicos(Map.of("login", dto.login(), "reason", "NOT_FOUND"))
                                    .build(),
                            request);
                    return new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
                });

        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getStatus() != UserStatusEnum.ACTIVE) {
            auditLogService.log(AuditEventRequest.builder()
                            .actorUserId(user.getId())
                            .tipoAuditoria(AuditScopeEnum.SYSTEM)
                            .modulo("Sistema")
                            .feature("Login")
                            .entidadePrincipal("Usuario")
                            .entidadeId(String.valueOf(user.getId()))
                            .acao("LOGIN")
                            .resultado(AuditResultEnum.FALHA)
                            .resumo("Falha de login para '" + user.getEmail() + "'")
                            .descricao("Tentativa de acesso negada porque o usuario esta inativo ou bloqueado.")
                            .detalhesTecnicos(Map.of("login", dto.login(), "reason", "INACTIVE_OR_DISABLED"))
                            .build(),
                    request);
            throw new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            auditLogService.log(AuditEventRequest.builder()
                            .actorUserId(user.getId())
                            .tipoAuditoria(AuditScopeEnum.SYSTEM)
                            .modulo("Sistema")
                            .feature("Login")
                            .entidadePrincipal("Usuario")
                            .entidadeId(String.valueOf(user.getId()))
                            .acao("LOGIN")
                            .resultado(AuditResultEnum.FALHA)
                            .resumo("Falha de login para '" + user.getEmail() + "'")
                            .descricao("Tentativa de acesso negada por senha informada incorretamente.")
                            .detalhesTecnicos(Map.of("login", dto.login(), "reason", "PASSWORD_MISMATCH"))
                            .build(),
                    request);
            throw new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
        }

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
                        .resumo("Login realizado por " + user.getFullName())
                        .descricao("Acesso autenticado com sucesso no sistema.")
                        .depois(Map.of("email", user.getEmail(), "role", user.getRole()))
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

    private AuthUserResponseDTO toAuthUserDTO(AppUser user) {
        return new AuthUserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStatus()
        );
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
