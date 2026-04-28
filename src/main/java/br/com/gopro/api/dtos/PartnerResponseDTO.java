package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PartnersTypeEnum;

import java.time.LocalDateTime;

public record PartnerResponseDTO(
        Long id,
        String acronym,
        String name,
        String tradeName,
        PartnersTypeEnum partnersType,
        String cnpj,
        String email,
        String phone,
        String address,
        String site,
        String city,
        String state,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy
) {}
