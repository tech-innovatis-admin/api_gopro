package br.com.gopro.api.dtos;

import java.time.LocalDate;

public record PeopleResponseDTO(
        Long id,
        String fullName,
        String cpf,
        String email,
        String phone,
        String avatarUrl,
        LocalDate birthDate,
        String address,
        String zipCode,
        String city,
        String state,
        String notes,
        Boolean isActive
) {
}
