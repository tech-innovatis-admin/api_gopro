package br.com.gopro.api.dtos;

public record OrganizationUpdateDTO(
        String name,
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
        Long updatedBy
) {}