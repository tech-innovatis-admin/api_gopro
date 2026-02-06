package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record OrganizationResponseDTO(
        Long id,
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
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}