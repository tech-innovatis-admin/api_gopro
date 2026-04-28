package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthForgotPasswordRequestDTO(
        @Email(message = "E-mail invalido")
        @NotBlank(message = "E-mail e obrigatorio")
        String email
) {
}
