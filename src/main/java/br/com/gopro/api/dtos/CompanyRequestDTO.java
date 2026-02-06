package br.com.gopro.api.dtos;

import jakarta.validation.constraints.NotBlank;

public record CompanyRequestDTO(
        @NotBlank(message = "Nome e obrigatorio") String name,
        @NotBlank(message = "Nome fantasia e obrigatorio") String tradeName,
        @NotBlank(message = "CNPJ e obrigatorio") String cnpj,
        @NotBlank(message = "Email e obrigatorio") String email,
        @NotBlank(message = "Telefone e obrigatorio") String phone,
        @NotBlank(message = "Endereco e obrigatorio") String address,
        @NotBlank(message = "Cidade e obrigatoria") String city,
        @NotBlank(message = "Estado e obrigatorio") String state,
        Long createdBy
) {}