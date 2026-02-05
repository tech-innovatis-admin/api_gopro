package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PartnersTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PartnerRequestDTO(
        String acronym,
        @NotBlank(message = "Nome é obrigatório") String name,
        @NotBlank(message = "Nome fantasia é obrigatório") String tradeName,
        @NotNull(message = "Tipo de parceiro é obrigatório") PartnersTypeEnum partnersType,
        @NotBlank(message = "CNPJ é obrigatório")
        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        @Email(message = "E-mail é obrigatório") String email,
        @NotBlank(message = "Telefone é obrigatório")
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres") String phone,
        @NotBlank(message = "Endereço é obrigatório")
        @Size(max = 400, message = "Endereço deve ter no máximo 400 caracteres") String address,
        @Size(max = 300, message = "Site deve ter no máximo 300 caracteres") String site,
        @NotBlank(message = "Cidade é obrigatório")
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres") String city,
        @NotBlank(message = "Estado é obrigatório")
        @Size(max = 50, message = "Estado deve ter no máximo 50 caracteres") String state,
        Boolean isActive,
        Long createdBy
) {}