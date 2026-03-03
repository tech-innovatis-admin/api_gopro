package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AdminUserResponseDTO;
import br.com.gopro.api.dtos.AdminUserUpdateRequestDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.exception.BusinessException;
import br.com.gopro.api.exception.ResourceNotFoundException;
import br.com.gopro.api.model.AppUser;
import br.com.gopro.api.repository.AppUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final AppUserRepository appUserRepository;
    private final AuditLogService auditLogService;

    @Override
    public PageResponseDTO<AdminUserResponseDTO> listUsers(UserRoleEnum role, UserStatusEnum status, int page, int size) {
        if (page < 0) {
            throw new BusinessException("Pagina deve ser maior ou igual a 0");
        }
        if (size <= 0 || size > 100) {
            throw new BusinessException("Tamanho da pagina deve estar entre 1 e 100");
        }

        Specification<AppUser> specification = Specification.where(null);
        if (role != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (status != null) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<AppUser> usersPage = appUserRepository.findAll(specification, pageable);
        List<AdminUserResponseDTO> content = usersPage.getContent().stream()
                .map(this::toDTO)
                .toList();

        return new PageResponseDTO<>(
                content,
                usersPage.getNumber(),
                usersPage.getSize(),
                usersPage.getTotalElements(),
                usersPage.getTotalPages(),
                usersPage.isFirst(),
                usersPage.isLast()
        );
    }

    @Override
    public AdminUserResponseDTO updateUser(
            Long userId,
            AdminUserUpdateRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    ) {
        AppUser target = appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));

        if (dto == null || (dto.role() == null && dto.status() == null)) {
            throw new BusinessException("Informe ao menos role ou status para atualizar");
        }

        enforceRbac(actor, target, dto);

        Map<String, Object> before = snapshot(target);

        if (dto.role() != null) {
            target.setRole(dto.role());
        }
        if (dto.status() != null) {
            target.setStatus(dto.status());
            target.setIsActive(dto.status() != UserStatusEnum.DISABLED);
        }
        target.setUpdatedBy(actor.id());

        AppUser saved = appUserRepository.save(target);
        auditLogService.log(
                actor.id(),
                AuditActions.USER_UPDATED,
                "users",
                String.valueOf(saved.getId()),
                before,
                snapshot(saved),
                request
        );

        return toDTO(saved);
    }

    private void enforceRbac(AuthenticatedUserPrincipal actor, AppUser target, AdminUserUpdateRequestDTO dto) {
        if (actor.role() == UserRoleEnum.SUPERADMIN) {
            return;
        }

        if (actor.role() != UserRoleEnum.ADMIN) {
            throw new AccessDeniedException("Acesso negado");
        }

        if (target.getRole() == UserRoleEnum.SUPERADMIN) {
            throw new AccessDeniedException("ADMIN nao pode alterar SUPERADMIN");
        }

        if (dto.role() == UserRoleEnum.SUPERADMIN) {
            throw new AccessDeniedException("ADMIN nao pode promover usuario para SUPERADMIN");
        }
    }

    private AdminUserResponseDTO toDTO(AppUser user) {
        return new AdminUserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.getStatus(),
                user.getIsActive(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private Map<String, Object> snapshot(AppUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("fullName", user.getFullName());
        map.put("role", user.getRole());
        map.put("status", user.getStatus());
        map.put("isActive", user.getIsActive());
        return map;
    }
}
