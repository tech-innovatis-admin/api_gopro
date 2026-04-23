package br.com.gopro.api.dtos;

public record CompanyResponsiblePersonDTO(
        Long id,
        String fullName,
        String cpf,
        String email
) {}
