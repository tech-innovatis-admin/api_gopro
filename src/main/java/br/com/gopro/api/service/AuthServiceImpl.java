package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthUserResponseDTO;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
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
                    auditLogService.log(
                            null,
                            AuditActions.LOGIN_FAILED,
                            "users",
                            null,
                            null,
                            Map.of("login", dto.login(), "reason", "NOT_FOUND"),
                            request
                    );
                    return new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
                });

        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getStatus() != UserStatusEnum.ACTIVE) {
            auditLogService.log(
                    user.getId(),
                    AuditActions.LOGIN_FAILED,
                    "users",
                    String.valueOf(user.getId()),
                    null,
                    Map.of("login", dto.login(), "reason", "INACTIVE_OR_DISABLED"),
                    request
            );
            throw new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
        }

        if (!passwordEncoder.matches(dto.password(), user.getPasswordHash())) {
            auditLogService.log(
                    user.getId(),
                    AuditActions.LOGIN_FAILED,
                    "users",
                    String.valueOf(user.getId()),
                    null,
                    Map.of("login", dto.login(), "reason", "PASSWORD_MISMATCH"),
                    request
            );
            throw new UnauthorizedException(GENERIC_INVALID_CREDENTIALS);
        }

        user.setLastLoginAt(LocalDateTime.now());
        appUserRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        AuthUserResponseDTO userDto = toAuthUserDTO(user);

        auditLogService.log(
                user.getId(),
                AuditActions.LOGIN_SUCCESS,
                "users",
                String.valueOf(user.getId()),
                null,
                Map.of("email", user.getEmail(), "role", user.getRole()),
                request
        );

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
