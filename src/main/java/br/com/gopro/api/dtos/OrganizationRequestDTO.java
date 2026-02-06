package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record OrganizationRequestDTO(
        @NotBlank(message = "Nome e obrigatorio") String name,
        String tradeName,
        String cnpj,
        Short type,
        String email,
        String phone,
        String address,
        String contactPerson,
        String zipCode,
        String city,
        String state,
        String notes,
        Long createdBy
) {}