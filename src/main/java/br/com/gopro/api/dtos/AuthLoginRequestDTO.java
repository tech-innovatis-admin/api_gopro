package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record AuthLoginRequestDTO(
        @NotBlank(message = "Login e obrigatorio") String login,
        @NotBlank(message = "Senha e obrigatoria") String password
) {
}
