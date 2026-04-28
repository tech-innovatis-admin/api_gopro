package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmailTestRequestDTO(
        @Email(message = "E-mail invalido")
        @NotBlank(message = "E-mail e obrigatorio")
        String email,

        @Size(max = 255, message = "Nome deve ter no maximo 255 caracteres")
        String recipientName,

        @NotBlank(message = "Assunto e obrigatorio")
        @Size(max = 255, message = "Assunto deve ter no maximo 255 caracteres")
        String subject,

        @NotBlank(message = "Mensagem e obrigatoria")
        @Size(max = 5000, message = "Mensagem deve ter no maximo 5000 caracteres")
        String message
) {
}
