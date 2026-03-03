package br.com.gopro.api.service;

import br.com.gopro.api.config.AuthenticatedUserPrincipal;
import br.com.gopro.api.dtos.AdminUserResponseDTO;
import br.com.gopro.api.dtos.AdminUserUpdateRequestDTO;
import br.com.gopro.api.dtos.PageResponseDTO;
import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import jakarta.servlet.http.HttpServletRequest;

public interface UserAdminService {

    PageResponseDTO<AdminUserResponseDTO> listUsers(UserRoleEnum role, UserStatusEnum status, int page, int size);

    AdminUserResponseDTO updateUser(
            Long userId,
            AdminUserUpdateRequestDTO dto,
            AuthenticatedUserPrincipal actor,
            HttpServletRequest request
    );
}
