package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PublicAgencyTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublicAgencyRequestDTO(
        @Size(max = 20, message = "Codigo deve ter no maximo 20 caracteres") String code,
        @Size(max = 20, message = "Sigla deve ter no maximo 20 caracteres") String sigla,
        @NotBlank(message = "Nome e obrigatorio") String name,
        @NotBlank(message = "CNPJ e obrigatorio")
        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        @NotNull(message = "E cliente e obrigatorio") Boolean isClient,
        @NotNull(message = "Tipo de orgao e obrigatorio") PublicAgencyTypeEnum publicAgencyType,
        @Email(message = "E-mail invalido") String email,
        @Size(max = 50, message = "Telefone deve ter no maximo 50 caracteres") String phone,
        @Size(max = 400, message = "Endereco deve ter no maximo 400 caracteres") String address,
        String contactPerson,
        @Size(max = 100, message = "Cidade deve ter no maximo 100 caracteres") String city,
        @Size(max = 50, message = "Estado deve ter no maximo 50 caracteres") String state,
        @NotNull(message = "Ativo e obrigatorio") Boolean isActive,
        Long createdBy) {
}
