package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record SecretaryUpdateDTO(
        String code,
        String sigla,
        Long publicAgencyId,
        String name,
        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        Boolean isClient,
        @Email(message = "E-mail inválido") String email,
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres") String phone,
        @Size(max = 400, message = "Endereço deve ter no máximo 400 caracteres") String address,
        String contactPerson,
        Boolean isActive,
        Long updatedBy) {
}
