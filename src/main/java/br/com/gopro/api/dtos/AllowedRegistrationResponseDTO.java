package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.AllowedRegistrationStatusEnum;
import br.com.gopro.api.enums.UserRoleEnum;

import java.time.LocalDateTime;

public record AllowedRegistrationResponseDTO(
        Long id,
        String email,
        UserRoleEnum role,
        AllowedRegistrationStatusEnum status,
        Long invitedByUserId,
        LocalDateTime invitedAt,
        LocalDateTime expiresAt,
        LocalDateTime usedAt,
        String inviteLink
) {
}
