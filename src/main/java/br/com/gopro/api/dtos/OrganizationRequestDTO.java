package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.OrganizationTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OrganizationRequestDTO(
        @NotBlank(message = "Nome é obrigatório!")
        String name,

        @NotBlank(message = "CNPJ é obrigatório!")
        String cnpj,

        OrganizationTypeEnum organizationType,

        @Email(message = "Email inválido!")
        String email,

        String phone,

        String address,

        String zipCode,

        String city,

        String state,

        String notes
) {
}
