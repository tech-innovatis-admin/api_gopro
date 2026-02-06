package br.com.gopro.api.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record PeopleResponseDTO(
        Long id,
        String fullName,
        String cpf,
        String email,
        String phone,
        LocalDate birthDate,
        String address,
        String zipCode,
        String city,
        String state,
        String notes,
        Boolean isActive
) {
}
