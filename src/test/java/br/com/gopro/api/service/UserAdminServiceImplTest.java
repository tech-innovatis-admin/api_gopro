package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AdminUserUpdateRequestDTO;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import br.com.gopro.api.service.audit.AuditEventRequest;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserAdminServiceImpl service;

    @Test
    void updateUser_shouldBlockAdminFromPromotingToSuperadmin() {
        AuthenticatedUserPrincipal admin = new AuthenticatedUserPrincipal(1L, "admin@empresa.com", UserRoleEnum.ADMIN);
        AppUser target = user(10L, UserRoleEnum.ANALISTA, UserStatusEnum.ACTIVE);
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(target));

        AdminUserUpdateRequestDTO dto = new AdminUserUpdateRequestDTO(UserRoleEnum.SUPERADMIN, null);

        assertThatThrownBy(() -> service.updateUser(10L, dto, admin, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ADMIN nao pode promover");
    }

    @Test
    void updateUser_shouldBlockAdminFromUpdatingExistingSuperadmin() {
        AuthenticatedUserPrincipal admin = new AuthenticatedUserPrincipal(1L, "admin@empresa.com", UserRoleEnum.ADMIN);
        AppUser target = user(10L, UserRoleEnum.SUPERADMIN, UserStatusEnum.ACTIVE);
        when(appUserRepository.findById(10L)).thenReturn(Optional.of(target));

        AdminUserUpdateRequestDTO dto = new AdminUserUpdateRequestDTO(UserRoleEnum.ADMIN, UserStatusEnum.DISABLED);

        assertThatThrownBy(() -> service.updateUser(10L, dto, admin, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ADMIN nao pode alterar SUPERADMIN");
    }

    @Test
    void updateUser_shouldAllowSuperadminToUpdateRoleAndStatus() {
        AuthenticatedUserPrincipal superadmin = new AuthenticatedUserPrincipal(1L, "root@empresa.com", UserRoleEnum.SUPERADMIN);
        AppUser target = user(10L, UserRoleEnum.ANALISTA, UserStatusEnum.ACTIVE);

        when(appUserRepository.findById(10L)).thenReturn(Optional.of(target));
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserUpdateRequestDTO dto = new AdminUserUpdateRequestDTO(UserRoleEnum.ADMIN, UserStatusEnum.DISABLED);

        var response = service.updateUser(10L, dto, superadmin, request);

        assertThat(response.role()).isEqualTo(UserRoleEnum.ADMIN);
        assertThat(response.status()).isEqualTo(UserStatusEnum.DISABLED);
        assertThat(response.isActive()).isFalse();
        ArgumentCaptor<AuditEventRequest> captor = ArgumentCaptor.forClass(AuditEventRequest.class);
        verify(auditLogService).log(captor.capture(), eq(request));

        AuditEventRequest event = captor.getValue();
        assertThat(event.getResumo()).isNull();
        assertThat(event.getDescricao()).isNull();
        assertThat(event.getDetalhesTecnicos()).isEqualTo(java.util.Map.of("auditAction", AuditActions.USER_UPDATED));
    }

    private AppUser user(Long id, UserRoleEnum role, UserStatusEnum status) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setEmail("user@empresa.com");
        user.setRole(role);
        user.setStatus(status);
        user.setIsActive(true);
        return user;
    }
}
