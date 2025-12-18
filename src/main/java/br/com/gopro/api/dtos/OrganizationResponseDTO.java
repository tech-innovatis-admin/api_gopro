package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.OrganizationTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OrganizationResponseDTO(
        Long id,
        String name,
        String cnpj,
        OrganizationTypeEnum organizationType,
        String email,
        String phone,
        String address,
        String zipCode,
        String city,
        String state,
        String notes
) {}
