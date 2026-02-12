package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PeopleRequestDTO(
        @NotBlank(message = "Nome completo e obrigatorio")
        String fullName,

        @NotBlank(message = "CPF e obrigatorio")
        String cpf,

        @Email(message = "Email invalido")
        String email,
        String phone,
        String avatarUrl,
        @NotNull(message = "Data de nascimento e obrigatoria")
        LocalDate birthDate,
        String address,
        @NotBlank(message = "CEP e obrigatorio")
        String zipCode,
        @NotBlank(message = "Cidade e obrigatoria")
        String city,
        @NotBlank(message = "Estado e obrigatorio")
        String state,
        String notes
) {
}
