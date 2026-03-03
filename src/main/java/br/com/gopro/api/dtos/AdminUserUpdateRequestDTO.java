package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;

public record AdminUserUpdateRequestDTO(
        UserRoleEnum role,
        UserStatusEnum status
) {
}
