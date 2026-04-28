package br.com.gopro.api.controller;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.config.SecurityPrincipalUtils;
import br.com.gopro.api.dtos.AdminUserResponseDTO;
import br.com.gopro.api.dtos.AdminUserUpdateRequestDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import br.com.gopro.api.service.UserAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Gestao administrativa de usuarios")
@PreAuthorize("hasAnyRole('OWNER','SUPERADMIN','ADMIN')")
public class AdminUserController {

    private final UserAdminService userAdminService;

    @Operation(summary = "Listar usuarios")
    @GetMapping
    public ResponseEntity<PageResponseDTO<AdminUserResponseDTO>> listUsers(
            @RequestParam(required = false) UserRoleEnum role,
            @RequestParam(required = false) UserStatusEnum status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(userAdminService.listUsers(role, status, page, size));
    }

    @Operation(summary = "Atualizar role/status de usuario")
    @PatchMapping("/{id}")
    public ResponseEntity<AdminUserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody AdminUserUpdateRequestDTO dto,
            Authentication authentication,
            HttpServletRequest request
    ) {
        AuthenticatedUserPrincipal actor = SecurityPrincipalUtils.require(authentication);
        return ResponseEntity.ok(userAdminService.updateUser(id, dto, actor, request));
    }
}
