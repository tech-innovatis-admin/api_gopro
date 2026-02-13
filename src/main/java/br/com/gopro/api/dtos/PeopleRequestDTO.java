package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PeopleRequestDTO(
        @NotBlank(message = "Nome completo e obrigatorio")
        String fullName,

        String cpf,

        @Email(message = "Email invalido")
        String email,
        String phone,
        String avatarUrl,
        LocalDate birthDate,
        String address,
        @NotBlank(message = "Cidade e obrigatoria")
        String city,
        @NotBlank(message = "Estado e obrigatorio")
        String state,
        String notes
) {
}
