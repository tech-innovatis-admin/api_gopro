package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.UserRoleEnum;
import br.com.gopro.api.enums.UserStatusEnum;

import java.time.LocalDateTime;

public record AdminUserResponseDTO(
        Long id,
        String username,
        String email,
        String fullName,
        UserRoleEnum role,
        UserStatusEnum status,
        Boolean isActive,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
