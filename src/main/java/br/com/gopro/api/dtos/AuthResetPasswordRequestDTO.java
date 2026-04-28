package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record AuthResetPasswordRequestDTO(
        @NotBlank(message = "Token e obrigatorio")
        String token,
        @NotBlank(message = "Senha e obrigatoria")
        String newPassword
) {
}
