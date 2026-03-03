package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record AllowedRegistrationReissueRequestDTO(
        LocalDateTime expiresAt
) {
}
