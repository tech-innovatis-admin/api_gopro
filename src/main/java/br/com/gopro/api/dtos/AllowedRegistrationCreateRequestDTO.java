package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.UserRoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AllowedRegistrationCreateRequestDTO(
        @Email(message = "E-mail invalido")
        @NotBlank(message = "E-mail e obrigatorio")
        String email,
        @NotNull(message = "Role e obrigatoria")
        UserRoleEnum role,
        LocalDateTime expiresAt
) {
}
