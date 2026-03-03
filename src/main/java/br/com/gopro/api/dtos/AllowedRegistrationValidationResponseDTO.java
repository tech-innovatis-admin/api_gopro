package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.UserRoleEnum;

import java.time.LocalDateTime;

public record AllowedRegistrationValidationResponseDTO(
        String email,
        UserRoleEnum role,
        LocalDateTime expiresAt
) {
}
