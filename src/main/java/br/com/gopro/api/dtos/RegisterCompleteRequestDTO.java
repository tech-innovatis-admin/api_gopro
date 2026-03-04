package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCompleteRequestDTO(
        @NotBlank(message = "Token e obrigatorio")
        String token,
        @NotBlank(message = "Nome completo e obrigatorio")
        @Size(max = 255, message = "Nome completo deve ter no maximo 255 caracteres")
        String fullName,
        @NotBlank(message = "Username e obrigatorio")
        @Size(max = 100, message = "Username deve ter no maximo 100 caracteres")
        String username,
        @NotBlank(message = "Senha e obrigatoria")
        String password
) {
}
