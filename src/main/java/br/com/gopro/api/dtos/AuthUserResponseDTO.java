package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;
import java.time.LocalDateTime;

public record AuthUserResponseDTO(
        Long id,
        String email,
        String username,
        String fullName,
        UserRoleEnum role,
        UserStatusEnum status,
        String avatarUrl,
        LocalDateTime lastLoginAt
) {
}
