package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record CompanyResponseDTO(
        Long id,
        String name,
        String tradeName,
        String cnpj,
        String email,
        String phone,
        String address,
        String city,
        String state,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}