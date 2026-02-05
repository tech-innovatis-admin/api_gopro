package br.com.gopro.api.dtos;

import java.time.LocalDateTime;

public record SecretaryResponseDTO(
        Long id,
        String code,
        String sigla,
        PublicAgencyResponseDTO publicAgency,
        String name,
        String cnpj,
        Boolean isClient,
        String email,
        String phone,
        String address,
        String contactPerson,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long createdBy,
        Long updatedBy) {
}
