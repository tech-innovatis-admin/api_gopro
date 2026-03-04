package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.JwtService;
import br.com.gopro.api.dtos.AllowedRegistrationCreateRequestDTO;
import br.com.gopro.api.dtos.AllowedRegistrationResponseDTO;
import br.com.gopro.api.dtos.RegisterCompleteRequestDTO;
import br.com.gopro.api.dtos.RegisterCompleteResponseDTO;
import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.model.AllowedRegistration;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AllowedRegistrationRepository;
import br.com.gopro.api.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllowedRegistrationServiceImplTest {

    @Mock
    private AllowedRegistrationRepository allowedRegistrationRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PasswordPolicyService passwordPolicyService;
    @Mock
    private RateLimitService rateLimitService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AllowedRegistrationServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "inviteBaseUrl", "http://localhost:3000/register");
        ReflectionTestUtils.setField(service, "defaultInviteExpirationHours", 72L);
        ReflectionTestUtils.setField(service, "registerRateLimitMaxAttempts", 12);
        ReflectionTestUtils.setField(service, "rateLimitWindowSeconds", 900L);
    }

    @Test
    void createInvite_shouldPersistTokenHash_andReturnRawTokenInLink() {
        when(allowedRegistrationRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(List.of());
        when(appUserRepository.findById(99L)).thenReturn(Optional.of(inviter(99L)));
        when(allowedRegistrationRepository.findByEmailIgnoreCase("analista@empresa.com"))
                .thenReturn(Optional.empty());
        when(allowedRegistrationRepository.save(any(AllowedRegistration.class)))
                .thenAnswer(invocation -> {
                    AllowedRegistration value = invocation.getArgument(0);
                    value.setId(1L);
                    return value;
                });

        AllowedRegistrationCreateRequestDTO dto = new AllowedRegistrationCreateRequestDTO(
                "analista@empresa.com",
                UserRoleEnum.ANALISTA,
                LocalDateTime.now().plusDays(2)
        );
        AuthenticatedUserPrincipal actor = new AuthenticatedUserPrincipal(99L, "admin@empresa.com", UserRoleEnum.SUPERADMIN);

        AllowedRegistrationResponseDTO response = service.createInvite(dto, actor, request);

        ArgumentCaptor<AllowedRegistration> captor = ArgumentCaptor.forClass(AllowedRegistration.class);
        verify(allowedRegistrationRepository).save(captor.capture());
        AllowedRegistration persisted = captor.getValue();

        String rawToken = extractTokenFromInviteLink(response.inviteLink());
        String expectedHash = sha256Hex(rawToken);

        assertThat(persisted.getInviteTokenHash()).isEqualTo(expectedHash);
        assertThat(persisted.getStatus()).isEqualTo(AllowedRegistrationStatusEnum.PENDING);
        assertThat(response.email()).isEqualTo("analista@empresa.com");
        assertThat(response.inviteLink()).contains("token=");
    }

    @Test
    void completeRegistration_shouldCreateUserMarkInviteUsed_andReturnToken() {
        String rawToken = "token-123";
        String hash = sha256Hex(rawToken);

        AllowedRegistration invite = pendingInvite(1L, "novo@empresa.com", hash, LocalDateTime.now().plusHours(4));
        invite.setInvitedByUser(inviter(10L));

        when(allowedRegistrationRepository.findByInviteTokenHashAndStatus(hash, AllowedRegistrationStatusEnum.PENDING))
                .thenReturn(Optional.of(invite));
        when(appUserRepository.existsByEmailIgnoreCase("novo@empresa.com")).thenReturn(false);
        when(appUserRepository.existsByUsernameIgnoreCase("novo.usuario")).thenReturn(false);
        when(passwordEncoder.encode("Senha@1234")).thenReturn("encoded-password");
        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> {
                    AppUser user = invocation.getArgument(0);
                    user.setId(50L);
                    return user;
                });
        when(allowedRegistrationRepository.save(any(AllowedRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(AppUser.class))).thenReturn("jwt-token");
        when(jwtService.getJwtExpirationSeconds()).thenReturn(3600L);

        RegisterCompleteRequestDTO dto = new RegisterCompleteRequestDTO(
                rawToken,
                "Novo Usuario",
                "novo.usuario",
                "Senha@1234"
        );

        RegisterCompleteResponseDTO response = service.completeRegistration(dto, request);

        assertThat(response.message()).isEqualTo("Cadastro concluido com sucesso");
        assertThat(response.auth()).isNotNull();
        assertThat(response.auth().accessToken()).isEqualTo("jwt-token");
        assertThat(invite.getStatus()).isEqualTo(AllowedRegistrationStatusEnum.USED);
        assertThat(invite.getUsedAt()).isNotNull();

        verify(allowedRegistrationRepository, atLeastOnce()).save(invite);
        verify(auditLogService, atLeast(2)).log(any(br.com.gopro.api.service.audit.AuditEventRequest.class), eq(request));
    }

    @Test
    void completeRegistration_shouldFailWhenInviteExpired() {
        String rawToken = "token-expired";
        String hash = sha256Hex(rawToken);
        AllowedRegistration invite = pendingInvite(2L, "expirado@empresa.com", hash, LocalDateTime.now().minusMinutes(1));

        when(allowedRegistrationRepository.findByInviteTokenHashAndStatus(hash, AllowedRegistrationStatusEnum.PENDING))
                .thenReturn(Optional.of(invite));
        when(allowedRegistrationRepository.save(any(AllowedRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterCompleteRequestDTO dto = new RegisterCompleteRequestDTO(
                rawToken,
                "Usuario Expirado",
                "usuario.expirado",
                "Senha@1234"
        );

        assertThatThrownBy(() -> service.completeRegistration(dto, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Convite invalido ou expirado");

        assertThat(invite.getStatus()).isEqualTo(AllowedRegistrationStatusEnum.EXPIRED);
        verify(allowedRegistrationRepository).save(invite);
    }

    @Test
    void completeRegistration_shouldFailWhenInviteAlreadyUsedOrInvalid() {
        String rawToken = "token-inexistente";
        String hash = sha256Hex(rawToken);

        when(allowedRegistrationRepository.findByInviteTokenHashAndStatus(hash, AllowedRegistrationStatusEnum.PENDING))
                .thenReturn(Optional.empty());

        RegisterCompleteRequestDTO dto = new RegisterCompleteRequestDTO(
                rawToken,
                "Usuario",
                "usuario",
                "Senha@1234"
        );

        assertThatThrownBy(() -> service.completeRegistration(dto, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Convite invalido ou expirado");
    }

    private AppUser inviter(Long id) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("inviter@empresa.com");
        user.setRole(UserRoleEnum.SUPERADMIN);
        return user;
    }

    private AllowedRegistration pendingInvite(Long id, String email, String hash, LocalDateTime expiresAt) {
        AllowedRegistration invite = new AllowedRegistration();
        invite.setId(id);
        invite.setEmail(email);
        invite.setRole(UserRoleEnum.ANALISTA);
        invite.setInviteTokenHash(hash);
        invite.setStatus(AllowedRegistrationStatusEnum.PENDING);
        invite.setInvitedAt(LocalDateTime.now().minusHours(1));
        invite.setExpiresAt(expiresAt);
        invite.setIsActive(true);
        return invite;
    }

    private String extractTokenFromInviteLink(String inviteLink) {
        String tokenPart = inviteLink.substring(inviteLink.indexOf("token=") + 6);
        return java.net.URLDecoder.decode(tokenPart, StandardCharsets.UTF_8);
    }

    private String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }
}
