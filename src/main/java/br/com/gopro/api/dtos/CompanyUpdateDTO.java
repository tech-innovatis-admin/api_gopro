package br.com.gopro.api.dtos;

public record CompanyUpdateDTO(
        String name,
        String tradeName,
        String cnpj,
        String email,
        String phone,
        String address,
        String city,
        String state,
        Long responsiblePersonId,
        Long updatedBy
) {}
