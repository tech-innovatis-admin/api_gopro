package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PublicAgencyTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record PublicAgencyUpdateDTO(
        @Size(max = 20, message = "Código deve ter no máximo 20 caracteres") String code,
        @Size(max = 20, message = "Sigla deve ter no máximo 20 caracteres") String sigla,
        String name,
        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        Boolean isClient,
        PublicAgencyTypeEnum publicAgencyType,
        @Email(message = "E-mail inválido") String email,
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres") String phone,
        @Size(max = 400, message = "Endereço deve ter no máximo 400 caracteres") String address,
        String contactPerson,
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres") String city,
        @Size(max = 50, message = "Estado deve ter no máximo 50 caracteres") String state,
        Boolean isActive,
        Long updatedBy) {
}
