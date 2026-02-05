package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PartnersTypeEnum;
import jakarta.validation.constraints.Size;

public record PartnerUpdateDTO(
        String acronym,
        String name,
        String tradeName,
        PartnersTypeEnum partnersType,
        @Size(min = 14, max = 14, message = "CNPJ deve ter 14 caracteres") String cnpj,
        String email,
        @Size(max = 50, message = "Telefone deve ter no máximo 50 caracteres") String phone,
        @Size(max = 400, message = "Endereço deve ter no máximo 400 caracteres") String address,
        @Size(max = 300, message = "Site deve ter no máximo 300 caracteres") String site,
        @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres") String city,
        @Size(max = 50, message = "Estado deve ter no máximo 50 caracteres") String state,
        Boolean isActive,
        Long updatedBy
) {}