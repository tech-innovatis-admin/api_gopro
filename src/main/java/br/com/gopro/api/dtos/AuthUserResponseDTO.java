package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;

public record AuthUserResponseDTO(
        Long id,
        String email,
        String username,
        String fullName,
        UserRoleEnum role,
        UserStatusEnum status
) {
}
