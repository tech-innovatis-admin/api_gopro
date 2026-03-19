package br.com.gopro.api.service;

import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private DocumentService documentService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "loginRateLimitMaxAttempts", 5);
        ReflectionTestUtils.setField(service, "rateLimitWindowSeconds", 900L);
    }

    @Test
    void login_shouldUseScopedRateLimitKeyAndResetAfterSuccess() {
        AppUser user = activeUser();
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10, 10.0.0.1");
        when(appUserRepository.findByLogin("Admin@Empresa.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Senha@123", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationSeconds()).thenReturn(3600L);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthLoginResponseDTO response = service.login(
                new AuthLoginRequestDTO("Admin@Empresa.com", "Senha@123"),
                request
        );

        String expectedKey = "login:203.0.113.10:admin@empresa.com";
        verify(rateLimitService).ensureWithinLimit(expectedKey, 5, 900L);
        verify(rateLimitService).reset(expectedKey);
        verify(rateLimitService, never()).registerAttempt(eq(expectedKey), eq(900L));
        assertThat(response.accessToken()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("admin@empresa.com");
    }

    @Test
    void login_shouldRegisterFailedAttemptOnlyForCurrentLoginKey() {
        AppUser user = activeUser();
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.10.10.10");
        when(appUserRepository.findByLogin("admin")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new AuthLoginRequestDTO("admin", "errada"), request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Credenciais invalidas");

        String expectedKey = "login:10.10.10.10:admin";
        verify(rateLimitService).ensureWithinLimit(expectedKey, 5, 900L);
        verify(rateLimitService).registerAttempt(expectedKey, 900L);
        verify(rateLimitService, never()).reset(expectedKey);
    }

    private AppUser activeUser() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setEmail("admin@empresa.com");
        user.setUsername("admin");
        user.setFullName("Administrador");
        user.setPasswordHash("hash");
        user.setRole(UserRoleEnum.ADMIN);
        user.setStatus(UserStatusEnum.ACTIVE);
        user.setIsActive(true);
        return user;
    }
}
