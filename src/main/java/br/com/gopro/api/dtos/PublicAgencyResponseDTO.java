package br.com.gopro.api.dtos;

import br.com.gopro.api.enums.PublicAgencyTypeEnum;

import java.time.LocalDateTime;

public record PublicAgencyResponseDTO(
        Long id,
        String code,
        String sigla,
        String name,
        String cnpj,
        Boolean isClient,
        PublicAgencyTypeEnum publicAgencyType,
        String email,
        String phone,
        String address,
        String contactPerson,
        String city,
        String state,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy) {
}
