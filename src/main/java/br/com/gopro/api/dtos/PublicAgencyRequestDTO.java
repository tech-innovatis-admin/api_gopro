package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PublicAgencyTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PublicAgencyRequestDTO(
        @Size(max = 20, message = "Código deve ter no máximo 20 caracteres") String code,
        @Size(max = 20, message = "Sigla deve ter no máximo 20 caracteres") String sigla,
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "CNPJ é obrigatório") @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        @NotNull(message = "É cliente é obrigatório") Boolean isClient,
        @NotNull(message = "Tipo de órgão público é obrigatório") PublicAgencyTypeEnum publicAgencyType,
        @Email(message = "E-mail inválido") String email,
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres") String phone,
        @Size(max = 400, message = "Endereço deve ter no máximo 400 caracteres") String address,
        String contactPerson,
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres") String city,
        @Size(max = 50, message = "Estado deve ter no máximo 50 caracteres") String state,
        @NotNull(message = "Ativo é obrigatório") Boolean isActive,
        Long createdBy) {
}
