package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SecretaryRequestDTO(
        String code,
        String sigla,
        @NotNull(message = "Órgão público é obrigatório") Long publicAgencyId,
        @NotBlank(message = "Nome é obrigatório") String name,
        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        @NotNull(message = "É cliente é obrigatório") Boolean isClient,
        @Email(message = "E-mail inválido") String email,
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres") String phone,
        @Size(max = 400, message = "Endereço deve ter no máximo 400 caracteres") String address,
        String contactPerson,
        Boolean isActive,
        Long createdBy) {
}
