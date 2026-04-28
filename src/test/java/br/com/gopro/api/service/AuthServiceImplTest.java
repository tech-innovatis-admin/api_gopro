package br.com.gopro.api.service;

import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AuthForgotPasswordRequestDTO;
import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AuthLoginRequestDTO;
import br.com.gopro.api.dtos.AuthLoginResponseDTO;
import br.com.gopro.api.dtos.AuthResetPasswordRequestDTO;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.model.PasswordResetToken;
import br.com.gopro.api.exception.UnauthorizedException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.repository.PasswordResetTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
    private PasswordPolicyService passwordPolicyService;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "loginRateLimitMaxAttempts", 5);
        ReflectionTestUtils.setField(service, "forgotPasswordRateLimitMaxAttempts", 3);
        ReflectionTestUtils.setField(service, "resetPasswordRateLimitMaxAttempts", 4);
        ReflectionTestUtils.setField(service, "rateLimitWindowSeconds", 900L);
        ReflectionTestUtils.setField(service, "passwordResetExpirationMinutes", 30L);
        ReflectionTestUtils.setField(service, "appUrl", "http://localhost:3000");
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
                .hasMessageContaining("Credenciais");

        String expectedKey = "login:10.10.10.10:admin";
        verify(rateLimitService).ensureWithinLimit(expectedKey, 5, 900L);
        verify(rateLimitService).registerAttempt(expectedKey, 900L);
        verify(rateLimitService, never()).reset(expectedKey);
    }

    @Test
    void forgotPassword_shouldThrowBusinessExceptionWhenEmailDoesNotExist() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10");
        when(appUserRepository.findByEmailIgnoreCase("ghost@empresa.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.forgotPassword(
                new AuthForgotPasswordRequestDTO("ghost@empresa.com"),
                request
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cadastrado");

        verify(rateLimitService).checkRateLimit("forgot-password:203.0.113.10:ghost@empresa.com", 3, 900L);
        verify(passwordResetTokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any(), any(), any());
    }

    @Test
    void forgotPassword_shouldPersistHashedTokenAndSendEmailForActiveUser() {
        AppUser user = activeUser();
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("10.0.0.9");
        when(appUserRepository.findByEmailIgnoreCase("admin@empresa.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.sendPasswordResetEmail(eq("admin@empresa.com"), eq("Administrador"), any(), any()))
                .thenReturn(new EmailService.EmailDispatchResult(true, 202, null, null, "ok"));

        var response = service.forgotPassword(new AuthForgotPasswordRequestDTO("Admin@Empresa.com"), request);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordResetTokenRepository).invalidateActiveTokensByUserId(1L, 1L);
        verify(passwordResetTokenRepository).save(tokenCaptor.capture());
        verify(emailService).sendPasswordResetEmail(eq("admin@empresa.com"), eq("Administrador"), linkCaptor.capture(), any());
        assertThat(tokenCaptor.getValue().getTokenHash()).hasSize(64);
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:3000/reset-password?token=");
        assertThat(tokenCaptor.getValue().getTokenHash()).doesNotContain("http://localhost:3000");
        assertThat(response.message()).contains("link de redefini");
    }

    @Test
    void forgotPassword_shouldThrowBusinessExceptionWhenEmailDispatchFails() {
        AppUser user = activeUser();
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10");
        when(appUserRepository.findByEmailIgnoreCase("admin@empresa.com")).thenReturn(Optional.of(user));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(emailService.sendPasswordResetEmail(eq("admin@empresa.com"), eq("Administrador"), any(), any()))
                .thenReturn(new EmailService.EmailDispatchResult(false, 400, null, null, "SendGrid nao configurado completamente"));

        assertThatThrownBy(() -> service.forgotPassword(
                new AuthForgotPasswordRequestDTO("admin@empresa.com"),
                request
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não foi possivel completar a operação");

        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("admin@empresa.com"), eq("Administrador"), any(), any());
    }

    @Test
    void resetPassword_shouldUpdatePasswordAndInvalidateExistingJwtSessions() {
        AppUser user = activeUser();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setId(9L);
        resetToken.setUser(user);
        resetToken.setTokenHash(sha256("raw-reset-token"));
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        resetToken.setIsActive(true);

        when(request.getHeader("X-Forwarded-For")).thenReturn("198.51.100.5");
        when(passwordResetTokenRepository.findByTokenHash(sha256("raw-reset-token"))).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("new-hash");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(passwordPolicyService).validateOrThrow("NovaSenha@123");

        var response = service.resetPassword(
                new AuthResetPasswordRequestDTO("raw-reset-token", "NovaSenha@123"),
                request
        );

        verify(rateLimitService).checkRateLimit("reset-password:198.51.100.5", 4, 900L);
        verify(passwordEncoder).encode("NovaSenha@123");
        verify(passwordResetTokenRepository).invalidateActiveTokensByUserId(1L, 1L);
        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(user.getAuthTokensInvalidBefore()).isNotNull();
        assertThat(resetToken.getUsedAt()).isNotNull();
        assertThat(resetToken.getIsActive()).isFalse();
        assertThat(response.message()).isEqualTo("Senha redefinida com sucesso");
    }
    
    @Test
    void updateMyAvatar_shouldBlockOwnerAccount() {
        AppUser owner = activeUser();
        owner.setRole(UserRoleEnum.OWNER);
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(owner));

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        assertThatThrownBy(() -> service.updateMyAvatar(
                new AuthenticatedUserPrincipal(1L, "owner@empresa.com", UserRoleEnum.OWNER),
                file
        ))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Acesso negado");
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

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
