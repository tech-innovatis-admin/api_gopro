package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PeopleRequestDTO(
        @NotBlank(message = "Nome completo é obrigatório!")
        String fullName,

        @NotBlank(message = "CPF é obrigatório!")
        String cpf,

        @Email(message = "Email inválido!")
        String email,
        String phone,
        LocalDate birthDate,
        String address,
        String zipCode,
        String city,
        String state,
        String notes
) {
}
